package com.infospica.dicom.command.batch;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import com.infospica.dicom.command.dcm4che.DeIdentifyCommand;
import org.apache.commons.exec.CommandLine;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.DicomEncodingOptions;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomOutputStream;

import com.infospica.dicom.command.CommandExecution;
import com.infospica.dicom.command.ModifyDicomCommand;
import com.infospica.dicom.command.executor.CommandExecutorProvider;
import com.infospica.dicom.command.pool.ExecutableCommandPool;
import com.infospica.dicom.config.iface.ExecutableCommand;
import com.infospica.dicom.config.iface.ResetableCommand;
import com.infospica.dicom.context.Context;
import com.infospica.dicom.util.LoggerUtility;

public class BatchModifyDicomCommand extends CommandExecution implements ExecutableCommand, ResetableCommand {

	private CommandLine commandLine;
	private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");  
	   
	public BatchModifyDicomCommand(ExecutableCommandPool<CommandExecutorProvider> executorProvider) {
		super(executorProvider);
	}

	@Override
	public String execute(Object... args) throws Exception {
		CommandLine cmdLine = getPreparedCommandLine();
		// Commented to replace commandline anonymization to DICOM implementation
		/*
		 * @SuppressWarnings("unchecked") Map<String, String> submap = (HashMap<String,
		 * String>)cmdLine.getSubstitutionMap(); submap.clear(); submap.put("filepath",
		 * (String)args[0]); submap.put("filepath_1", (String)args[1]); return
		 * executeCommandWithResult(cmdLine);
		 */
		List<String> processedFileList = new ArrayList<>();
		modifyMetadata((String) args[0], (String) args[1], processedFileList);
		return processedFileList.stream().collect(Collectors.joining("\r\n"));
	}

	@Override
	public void reset() {
		this.commandLine = null;
	}

	private CommandLine getPreparedCommandLine() {
		// https://github.com/dcm4che/dcm4che/blob/master/dcm4che-tool/dcm4che-tool-deidentify/README.md
		// https://cloud.google.com/healthcare-api/docs/how-tos/dicom-deidentify //
		// https://www.dcm4che.org/docs/dcm4che-2.0.17-apidocs/org/dcm4che2/data/Tag.html
		if (this.commandLine == null) {
			this.commandLine = new CommandLine(getExecutableCommand("deidentify"));
			this.commandLine.addArgument("--retain-uid");
			this.commandLine.addArgument("--retain-date");
			this.commandLine.addArgument("--retain-dev");
			this.commandLine.addArgument("--retain-org");
			this.commandLine.addArgument("--retain-pid-hash");
			if (Context.isAnonymizePatientName()) {
				this.commandLine.addArgument("-sPatientName=\"*******\"", false);
			}
			if (Context.isAnonymizePatientId()) {
				this.commandLine.addArgument("-sPatientID=\"*******\"", false);
			}
			if (Context.isAnonymizeReferringPhysician()) {
				this.commandLine.addArgument("-sReferringPhysicianName=\"*******\"", false);
			}
			if (Context.isAnonymizeInstitutionName()) {
				this.commandLine.addArgument("-sInstitutionName=\"*******\"", false);
			}
			if (Context.isAnonymizeInstitutionAddress()) {
				this.commandLine.addArgument("-sInstitutionAddress=\"*******\"", false);
			}
			this.commandLine.addArgument("--");
			this.commandLine.addArgument("${filepath}");
			this.commandLine.addArgument("${filepath_1}");
			this.commandLine.setSubstitutionMap(new HashMap<String, String>());
		}
		return this.commandLine;
	}

	private void modifyMetadata(String inputDicomDir, String anonymizeDir, List<String> processedFileList) {
		File[] dicomFiles = new File(inputDicomDir).listFiles();
		String replaceString = "**********";
		DicomEncodingOptions dicomEncodingOptions = DicomEncodingOptions.DEFAULT;
		for (File dicomFile : dicomFiles) {
			if (dicomFile.isDirectory()) {
				modifyMetadata(dicomFile.getAbsolutePath(), anonymizeDir, processedFileList);
			} else {
				LoggerUtility.log(BatchModifyDicomCommand.class, LoggerUtility.LogLevel.INFO, dicomFile.getAbsolutePath() + "[Dicom]");
				if(dicomFile.exists() && dicomFile.getAbsolutePath().indexOf("part") >= 0) {
					LoggerUtility.log(BatchModifyDicomCommand.class, LoggerUtility.LogLevel.INFO, "################## Partly Stored File hence Skipping ###########" + dicomFile.getAbsolutePath());
					continue;
				}
				String anonymizeFile = anonymizeDir + File.separator + dicomFile.getName();
				try {
					DeIdentifyCommand deidentifier = new DeIdentifyCommand(getOptions());
					Attributes fmi;
					Attributes dataset;
					try (DicomInputStream dis = new DicomInputStream(dicomFile)) {
						LoggerUtility.log(BatchModifyDicomCommand.class, LoggerUtility.LogLevel.INFO, "##################Anonymizing###########" + dicomFile.getAbsolutePath());
						dis.setIncludeBulkData(DicomInputStream.IncludeBulkData.URI);
						fmi = dis.readFileMetaInformation();
						dataset = dis.readDataset(-1, -1);
					}
					anonymizeAttributes(deidentifier, dataset, replaceString);
					deidentifier.deidentify(dataset);
					if (fmi != null)
						fmi = dataset.createFileMetaInformation(fmi.getString(Tag.TransferSyntaxUID));
					try(DicomOutputStream dos = new DicomOutputStream(new File(anonymizeDir + File.separator + dicomFile.getName()))) {
						dos.setEncodingOptions(dicomEncodingOptions);
						dos.writeDataset(fmi, dataset);
					}
					processedFileList.add(dicomFile.getAbsolutePath());
				} catch (IOException ioe) {
					LoggerUtility.log(BatchModifyDicomCommand.class, LoggerUtility.LogLevel.INFO, "NOT A VALID DICOM FILE.." + ioe);
					processedFileList.add("Failed to de-identify " + dicomFile.getAbsolutePath() + ":");
				}
				LoggerUtility.log(BatchModifyDicomCommand.class, LoggerUtility.LogLevel.INFO, anonymizeFile + "[Anonymized] at " + dtf.format(LocalDateTime.now()));
			}
		}
	}

	private void anonymizeAttributes(DeIdentifyCommand deidentifier, Attributes dataset, String replaceString) {
		if (Context.isAnonymizePatientName()) {
			deidentifier.setDummyValue(Tag.PatientName, VR.PN, replaceString);
		}
		if (Context.isAnonymizePatientId() ) {
			deidentifier.setDummyValue(Tag.PatientID, VR.LO, replaceString);
		}
		if (Context.isAnonymizeReferringPhysician()) {
			deidentifier.setDummyValue(Tag.ReferringPhysicianName, VR.PN, replaceString);
		}
		if (Context.isAnonymizeInstitutionName()) {
			deidentifier.setDummyValue(Tag.InstitutionName, VR.LO, replaceString);
		}
		if (Context.isAnonymizeInstitutionAddress()) {
			deidentifier.setDummyValue(Tag.InstitutionAddress, VR.ST, replaceString);
		}
	}

	private DeIdentifyCommand.Option[] getOptions() {
		EnumSet<DeIdentifyCommand.Option> options = EnumSet.noneOf(DeIdentifyCommand.Option.class);
		options.add(DeIdentifyCommand.Option.RetainLongitudinalTemporalInformationFullDatesOption);
		options.add(DeIdentifyCommand.Option.RetainDeviceIdentityOption);
		//options.add(DeIdentifyCommand.Option.RetainInstitutionIdentityOption);
		options.add(DeIdentifyCommand.Option.RetainUIDsOption);
		//options.add(DeIdentifier.Option.RetainPatientIDHashOption);
		return options.toArray(new DeIdentifyCommand.Option[0]);
	}
}



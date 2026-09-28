package com.infospica.dicom.command;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.exec.CommandLine;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomOutputStream;

import com.infospica.dicom.command.batch.BatchModifyDicomCommand;
import com.infospica.dicom.command.executor.CommandExecutorProvider;
import com.infospica.dicom.command.pool.ExecutableCommandPool;
import com.infospica.dicom.config.iface.ExecutableCommand;
import com.infospica.dicom.config.iface.ResetableCommand;
import com.infospica.dicom.context.Context;
import com.infospica.dicom.util.FileUtility;
import com.infospica.dicom.util.LoggerUtility;

public class ModifyDicomCommand extends CommandExecution implements ExecutableCommand, ResetableCommand {

	private CommandLine commandLine;
	private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");  
    public ModifyDicomCommand(ExecutableCommandPool<CommandExecutorProvider> executorProvider) {
		super(executorProvider);
	}

	@Override
	public String execute(Object... args) throws Exception {
		CommandLine cmdLine = getPreparedCommandLine();
		@SuppressWarnings("unchecked")
		Map<String, String> submap = (HashMap<String, String>) cmdLine.getSubstitutionMap();
		String anonymizedFile = getAnonymizedFile((String) args[0]);
		// Commented to replace commandline anonymization to DICOM implementation
		/*
		 * submap.clear(); submap.put("filepath", (String)args[0]);
		 * submap.put("filepath_1", anonymizedFile); int exitCode =
		 * executeCommand(cmdLine); if(exitCode > 0) throw new
		 * ModificationException("Error modifying the file: " + args[0]);
		 */
		List<String> processedFileList = new ArrayList<>();
		modifyMetadata((String) args[0], anonymizedFile, processedFileList);
		return processedFileList.stream().collect(Collectors.joining("\r\n"));

	}

	@Override
	public void reset() {
		this.commandLine = null;
	}

	private CommandLine getPreparedCommandLine() {
		// https://github.com/dcm4che/dcm4che/blob/master/dcm4che-tool/dcm4che-tool-deidentify/README.md
		// https://cloud.google.com/healthcare-api/docs/how-tos/dicom-deidentify //
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

	private String getAnonymizedFile(String srcFile) {
		File anonymizeFolder = new File(Context.getAnonymizedDestinationFolder(),
				FileUtility.getRelativePathFrom(srcFile, Context.getAnonymizedSourceFolder().getAbsolutePath()));
		if (!anonymizeFolder.getParentFile().exists()) {
			anonymizeFolder.getParentFile().mkdirs();
		}
		LoggerUtility.log(ModifyDicomCommand.class, LoggerUtility.LogLevel.INFO,
				"Considering the anonymized folder on " +dtf.format(LocalDateTime.now())+":"+ anonymizeFolder.getAbsolutePath());
		return anonymizeFolder.getAbsolutePath();
	}

	private void modifyMetadata(String inputDicomDir, String anonymizeDir, List<String> processedFileList) {
		File[] dicomFiles = new File(inputDicomDir).listFiles();
		String replaceString = "**********";
		for (File dicomFile : dicomFiles) {
			if (dicomFile.isDirectory()) {
				modifyMetadata(dicomFile.getAbsolutePath(), anonymizeDir, processedFileList);
			} else {
				LoggerUtility.log(BatchModifyDicomCommand.class, LoggerUtility.LogLevel.INFO, dicomFile.getAbsolutePath() + "[Dicom]");
				if(dicomFile.exists() && dicomFile.getAbsolutePath().indexOf("part") >= 0) {
					LoggerUtility.log(BatchModifyDicomCommand.class, LoggerUtility.LogLevel.INFO, "################## Partly Stored File hence Skipping ###########" + dicomFile.getAbsolutePath());
					continue;
				}
				try (DicomInputStream dis = new DicomInputStream(dicomFile)) {

					LoggerUtility.log(BatchModifyDicomCommand.class, LoggerUtility.LogLevel.INFO, "##################Anonymizing###########" + dicomFile.getAbsolutePath());
					Attributes attributes = dis.readDataset();
					if (Context.isAnonymizePatientName() && attributes.contains(Tag.PatientName)) {
						attributes.setString(Tag.PatientName, VR.PN, replaceString);
					}
					if (Context.isAnonymizePatientId() && attributes.contains(Tag.PatientID)) {
						attributes.setString(Tag.PatientID, VR.LO, replaceString);
					}
					if (Context.isAnonymizeReferringPhysician() && attributes.contains(Tag.ReferringPhysicianName)) {
						attributes.setString(Tag.ReferringPhysicianName, VR.PN, replaceString);
					}
					if (Context.isAnonymizeInstitutionName() && attributes.contains(Tag.InstitutionName)) {
						attributes.setString(Tag.InstitutionName, VR.LO, replaceString);
					}
					if (Context.isAnonymizeInstitutionAddress() && attributes.contains(Tag.InstitutionAddress)) {
						attributes.setString(Tag.InstitutionAddress, VR.ST, replaceString);
					}
					String anonymizeFile = anonymizeDir + File.separator + dicomFile.getName();
					LoggerUtility.log(BatchModifyDicomCommand.class, LoggerUtility.LogLevel.INFO, anonymizeFile + "[Anonymized] at " + dtf.format(LocalDateTime.now()));

					try(DicomOutputStream dos = new DicomOutputStream(new File(anonymizeDir + File.separator + dicomFile.getName()))) {
						attributes.writeTo(dos);
					}
					processedFileList.add(dicomFile.getAbsolutePath());
				} catch (IOException ioe) {
					LoggerUtility.log(BatchModifyDicomCommand.class, LoggerUtility.LogLevel.INFO, "NOT A VALID DICOM FILE.." + ioe);
					processedFileList.add("Failed to de-identify " + dicomFile.getAbsolutePath() + ":");
				}
			}
		}
	}
}

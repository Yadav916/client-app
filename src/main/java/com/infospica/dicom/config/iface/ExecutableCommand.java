package com.infospica.dicom.config.iface;

public interface ExecutableCommand {

    public String execute(Object...args) throws Exception;
}

package com.infospica.dicom.config.iface;

public interface Restorable<T> {

    void restore(T obj);
}

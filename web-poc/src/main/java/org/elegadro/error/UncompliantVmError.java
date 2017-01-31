package org.elegadro.error;

import java.io.UnsupportedEncodingException;

/**
 * @author Taimo Peelo
 */
public final class UncompliantVmError extends VirtualMachineError {
    public UncompliantVmError(String message, Throwable cause) {
        super(message, cause);
    }

    public static UncompliantVmError UTF_8_unsupported(UnsupportedEncodingException ex) {
        return new UncompliantVmError("No UTF-8 in VM.", ex);
    }
}

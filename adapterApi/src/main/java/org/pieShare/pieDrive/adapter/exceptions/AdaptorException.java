/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pieShare.pieDrive.adapter.exceptions;

/**
 *
 * @author richy
 */
public class AdaptorException extends Exception {

    public AdaptorException(String message) {
        super(message);
    }
    
    
    public AdaptorException(Throwable throwable) {
        super(throwable);
    }
}

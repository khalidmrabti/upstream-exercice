package com.exercice.exercice.exceptions;

import com.exercice.exercice.model.PaymentStatus;

public class TransactionException extends Exception{
    public TransactionException(String s) {
        super(s);
    }

    public static Exception cannotFind(String id){
        return new TransactionException("Couldn't find the giving transaction '"+id+"'");
    }

    public static Exception cannotUpdateCaptured(){
        return new TransactionException("Cannot modify a captured transaction.");
    }

    public static Exception cannotDeleteCaptured() {
        return new TransactionException("Cannot delete a captured transaction.");
    }

    public static Exception cannotCaptureUnAuthorized(){
        return new TransactionException("Cannot change the status of the payment to: "+ PaymentStatus.CAPTURED.name()+" unless it is "+PaymentStatus.AUTHORIZED.name());
    }
}

package com.artificesoft.labseven.samples.readwrite.protocol;

public enum TransactionState {
    SUCCESS,
    FAILURE_COST_CHANGED,
    FAILURE_COSTS_TOO_MUCH,
    FAILURE_ALREADY_PURCHASED,
    FAILURE_TIMED_OUT
}

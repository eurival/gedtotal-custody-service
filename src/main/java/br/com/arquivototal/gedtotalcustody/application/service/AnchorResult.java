package br.com.arquivototal.gedtotalcustody.application.service;

public record AnchorResult(String txHash, String network, String contractAddress, boolean simulated) {}

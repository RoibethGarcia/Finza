package com.gestorgastos.ledger.application.service;

import java.util.UUID;

public record TransferView(
	UUID transferGroupId,
	TransactionView outgoingTransaction,
	TransactionView incomingTransaction
) {
}

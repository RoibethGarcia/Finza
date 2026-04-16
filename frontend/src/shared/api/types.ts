export type UserStatus = 'ACTIVE' | 'INACTIVE';

export type AuthSession = {
  tokenType: string;
  accessToken: string;
  accessTokenExpiresAt: string;
  refreshToken: string;
  refreshTokenExpiresAt: string;
  user: UserProfile;
};

export type UserProfile = {
  id: string;
  fullName: string;
  email: string;
  birthDate: string | null;
  status: UserStatus;
};

export type AccountType = 'CASH' | 'BANK' | 'SAVINGS' | 'CREDIT_LINE';

export type Account = {
  id: string;
  name: string;
  type: AccountType;
  currency: string;
  openingBalance: number;
  archived: boolean;
  createdAt: string;
  updatedAt: string;
};

export type CategoryType = 'INCOME' | 'EXPENSE';

export type Category = {
  id: string;
  name: string;
  type: CategoryType;
  archived: boolean;
  createdAt: string;
  updatedAt: string;
};

export type TransactionType = 'INCOME' | 'EXPENSE' | 'TRANSFER_IN' | 'TRANSFER_OUT' | 'ADJUSTMENT';

export type Transaction = {
  id: string;
  accountId: string;
  accountName: string | null;
  categoryId: string | null;
  categoryName: string | null;
  type: TransactionType;
  amount: number;
  currency: string;
  occurredAt: string;
  description: string | null;
  transferGroupId: string | null;
};

export type DashboardSummary = {
  periodStart: string;
  periodEnd: string;
  currencies: Array<{
    currency: string;
    totalIncome: number;
    totalExpense: number;
    netAmount: number;
    availableBalance: number;
  }>;
  accounts: Array<{
    accountId: string;
    accountName: string;
    currency: string;
    openingBalance: number;
    currentBalance: number;
    periodIncome: number;
    periodExpense: number;
    periodNet: number;
  }>;
  recentTransactions: Array<{
    transactionId: string;
    accountId: string;
    accountName: string | null;
    categoryId: string | null;
    categoryName: string | null;
    type: TransactionType;
    amount: number;
    currency: string;
    occurredAt: string;
    description: string | null;
  }>;
};

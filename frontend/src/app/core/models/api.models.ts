export type Role = 'ADMIN' | 'FINANCE_USER' | 'AUDITOR' | 'OPERATIONS_USER';

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  username: string;
  role: Role;
}

export interface SessionUser {
  username: string;
  role: Role;
  accessToken: string;
}

export interface PageResponse<T> {
  content: T[];
  page?: {
    size: number;
    number: number;
    totalElements: number;
    totalPages: number;
  };
}

export interface Depot {
  id: number;
  code: string;
  name: string;
  active: boolean;
  createdAt: string;
  updatedAt: string;
  version: number;
}

export interface Terminal {
  id: number;
  terminalNumber: string;
  serialNumber: string;
  status: string;
  lastSyncTime: string | null;
  lastTransactionTime: string | null;
  pendingTransactionCount: number;
  retryCount: number;
  active: boolean;
  createdAt: string;
  updatedAt: string;
  version: number;
}

export interface Assignment {
  id: number;
  terminalId: number;
  depotId: number;
  validFrom: string;
  validTo: string | null;
  createdAt: string;
}

export interface Transaction {
  id: number;
  transactionReference: string;
  approvalNumber: string;
  cardAlias: string;
  terminalId: number;
  transactionType: string;
  productType: string | null;
  amount: number;
  transactionTime: string;
  createdAt: string;
  sourceSystem: string;
}

export interface ReconciliationResult {
  id: number;
  financialPeriodId: number;
  expectedClosingBalance: number;
  actualClosingBalance: number;
  variance: number;
  saleAmount: number;
  cancellationAmount: number;
  netAmount: number;
  status: string;
  resolutionNote: string | null;
  resolvedBy: string | null;
  resolvedAt: string | null;
  createdAt: string;
  version: number;
}

export interface FinancialPeriod {
  id: number;
  depotId: number;
  periodDate: string;
  openingBalance: number;
  depositedAmount: number;
  withdrawalAmount: number;
  actualClosingBalance: number;
  status: string;
  createdAt: string;
  updatedAt: string;
  version: number;
}

export interface Anomaly {
  id: number;
  ruleCode: string;
  severity: string;
  status: string;
  entityType: string;
  entityId: string;
  title: string;
  details: string | null;
  detectedAt: string;
  reviewedBy: string | null;
  resolutionNote: string | null;
  resolvedAt: string | null;
  version: number;
}

export interface ReportSnapshot {
  id: number;
  reportType: string;
  parametersJson: string;
  status: 'RUNNING' | 'COMPLETED' | 'FAILED' | string;
  resultHash: string | null;
  outputPath: string | null;
  errorMessage: string | null;
  requestedBy: string | null;
  startedAt: string | null;
  completedAt: string | null;
  version: number;
}

export interface AuditLog {
  id: number;
  action: string;
  entityType: string;
  entityId: string;
  actorUsername: string;
  detailsJson: string | null;
  createdAt: string;
}

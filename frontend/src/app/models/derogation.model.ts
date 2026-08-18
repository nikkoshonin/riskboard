import { LimitType } from './risk-limit.model';

export type DerogationStatus = 'PENDING' | 'APPROVED' | 'REJECTED';

export interface DerogationRequest {
  id: number;
  counterpartyId: number;
  counterpartyName: string;
  limitType: LimitType;
  requestedBy: string;
  amount: number;
  reason: string;
  status: DerogationStatus;
  createdAt: string;
}

export interface CreateDerogationRequest {
  counterpartyId: number;
  limitType: LimitType;
  amount: number;
  reason: string;
  requestedBy: string;
}

export interface LimitCheck {
  limitExists: boolean;
  maxAmount: number | null;
  maxAllowedDerogationAmount: number | null;
}

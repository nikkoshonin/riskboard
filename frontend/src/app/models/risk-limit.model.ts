export type LimitType = 'CREDIT' | 'MARKET' | 'LIQUIDITY';
export type AlertLevel = 'GREEN' | 'ORANGE' | 'RED';

export interface RiskLimit {
  id: number;
  counterpartyId: number;
  counterpartyName: string;
  sector: string;
  limitType: LimitType;
  maxAmount: number;
  usedAmount: number;
  currency: string;
  usageRate: number;
  alertLevel: AlertLevel;
  lastUpdated: string;
}

export interface SectorExposure {
  sector: string;
  totalUsedAmount: number;
}

export interface Counterparty {
  id: number;
  name: string;
  ricosCode: string;
  country: string;
  sector: string;
}

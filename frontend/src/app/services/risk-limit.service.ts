import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { Counterparty, LimitType, RiskLimit, SectorExposure } from '../models/risk-limit.model';
import { ImportResult } from '../models/import-result.model';

const API_BASE = '/api';

@Injectable({ providedIn: 'root' })
export class RiskLimitService {

  constructor(private http: HttpClient) {}

  getAllRiskLimits(): Observable<RiskLimit[]> {
    return this.http.get<RiskLimit[]>(`${API_BASE}/risk-limits`);
  }

  getExposureBySector(limitType: LimitType): Observable<SectorExposure[]> {
    return this.http.get<SectorExposure[]>(`${API_BASE}/risk-limits/exposure-by-sector`, {
      params: { limitType }
    });
  }

  getCounterparties(): Observable<Counterparty[]> {
    return this.http.get<Counterparty[]>(`${API_BASE}/risk-limits/counterparties`);
  }

  importCsv(file: File): Observable<ImportResult> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<ImportResult>(`${API_BASE}/import/csv`, formData);
  }
}

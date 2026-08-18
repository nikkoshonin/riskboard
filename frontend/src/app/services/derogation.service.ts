import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { CreateDerogationRequest, DerogationRequest, LimitCheck } from '../models/derogation.model';
import { LimitType } from '../models/risk-limit.model';

const API_BASE = '/api';

@Injectable({ providedIn: 'root' })
export class DerogationService {

  constructor(private http: HttpClient) {}

  checkLimit(counterpartyId: number, limitType: LimitType): Observable<LimitCheck> {
    return this.http.get<LimitCheck>(`${API_BASE}/derogations/check-limit`, {
      params: { counterpartyId, limitType }
    });
  }

  create(request: CreateDerogationRequest): Observable<DerogationRequest> {
    return this.http.post<DerogationRequest>(`${API_BASE}/derogations`, request);
  }

  getPending(): Observable<DerogationRequest[]> {
    return this.http.get<DerogationRequest[]>(`${API_BASE}/derogations/pending`);
  }

  approve(id: number): Observable<DerogationRequest> {
    return this.http.put<DerogationRequest>(`${API_BASE}/derogations/${id}/approve`, {});
  }

  reject(id: number): Observable<DerogationRequest> {
    return this.http.put<DerogationRequest>(`${API_BASE}/derogations/${id}/reject`, {});
  }
}

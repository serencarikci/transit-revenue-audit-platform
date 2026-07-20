import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  Anomaly,
  Assignment,
  AuditLog,
  Depot,
  FinancialPeriod,
  PageResponse,
  ReconciliationResult,
  ReportSnapshot,
  Terminal,
  Transaction,
} from '../models/api.models';

const base = environment.apiBaseUrl;

@Injectable({ providedIn: 'root' })
export class DepotApi {
  constructor(private readonly http: HttpClient) {}

  list(active?: boolean): Observable<PageResponse<Depot>> {
    let params = new HttpParams().set('size', '100');
    if (active !== undefined) {
      params = params.set('active', String(active));
    }
    return this.http.get<PageResponse<Depot>>(`${base}/depots`, { params });
  }

  create(body: { code: string; name: string }): Observable<Depot> {
    return this.http.post<Depot>(`${base}/depots`, body);
  }
}

@Injectable({ providedIn: 'root' })
export class TerminalApi {
  constructor(private readonly http: HttpClient) {}

  list(active?: boolean): Observable<PageResponse<Terminal>> {
    let params = new HttpParams().set('size', '200');
    if (active !== undefined) {
      params = params.set('active', String(active));
    }
    return this.http.get<PageResponse<Terminal>>(`${base}/terminals`, { params });
  }

  create(body: { terminalNumber: string; serialNumber: string }): Observable<Terminal> {
    return this.http.post<Terminal>(`${base}/terminals`, body);
  }

  assignments(id: number): Observable<Assignment[]> {
    return this.http.get<Assignment[]>(`${base}/terminals/${id}/assignments`);
  }

  createAssignment(id: number, body: { depotId: number; validFrom: string }): Observable<Assignment> {
    return this.http.post<Assignment>(`${base}/terminals/${id}/assignments`, body);
  }
}

@Injectable({ providedIn: 'root' })
export class TransactionApi {
  constructor(private readonly http: HttpClient) {}

  search(q: {
    terminalId?: number;
    type?: string;
    from?: string;
    to?: string;
    page?: number;
    size?: number;
  }): Observable<PageResponse<Transaction>> {
    let params = new HttpParams().set('page', String(q.page ?? 0)).set('size', String(q.size ?? 50));
    if (q.terminalId != null) params = params.set('terminalId', String(q.terminalId));
    if (q.type) params = params.set('type', q.type);
    if (q.from) params = params.set('from', q.from);
    if (q.to) params = params.set('to', q.to);
    return this.http.get<PageResponse<Transaction>>(`${base}/transactions`, { params });
  }
}

@Injectable({ providedIn: 'root' })
export class ReconciliationApi {
  constructor(private readonly http: HttpClient) {}

  createPeriod(body: {
    depotId: number;
    periodDate: string;
    openingBalance: number;
    depositedAmount: number;
    withdrawalAmount: number;
    actualClosingBalance: number;
  }): Observable<FinancialPeriod> {
    return this.http.post<FinancialPeriod>(`${base}/reconciliation/periods`, body);
  }

  calculate(id: number): Observable<ReconciliationResult> {
    return this.http.post<ReconciliationResult>(`${base}/reconciliation/periods/${id}/calculate`, {});
  }

  results(q?: {
    year?: number;
    month?: number;
    status?: string;
  }): Observable<PageResponse<ReconciliationResult>> {
    let params = new HttpParams();
    if (q?.year != null) params = params.set('year', q.year);
    if (q?.month != null) params = params.set('month', q.month);
    if (q?.status) params = params.set('status', q.status);
    return this.http.get<PageResponse<ReconciliationResult>>(`${base}/reconciliation/results`, { params });
  }

  resolve(id: number, body: { resolutionNote: string; version: number }): Observable<ReconciliationResult> {
    return this.http.post<ReconciliationResult>(`${base}/reconciliation/results/${id}/resolve`, body, {
      headers: { 'If-Match': String(body.version) },
    });
  }
}

@Injectable({ providedIn: 'root' })
export class AnomalyApi {
  constructor(private readonly http: HttpClient) {}

  search(q?: { severity?: string; status?: string }): Observable<Anomaly[]> {
    let params = new HttpParams();
    if (q?.severity) params = params.set('severity', q.severity);
    if (q?.status) params = params.set('status', q.status);
    return this.http.get<Anomaly[]>(`${base}/anomalies`, { params });
  }

  review(id: number): Observable<Anomaly> {
    return this.http.post<Anomaly>(`${base}/anomalies/${id}/review`, {});
  }

  resolve(id: number, body: { resolutionNote: string; version: number }): Observable<Anomaly> {
    return this.http.post<Anomaly>(`${base}/anomalies/${id}/resolve`, body, {
      headers: { 'If-Match': String(body.version) },
    });
  }
}

@Injectable({ providedIn: 'root' })
export class ReportingApi {
  constructor(private readonly http: HttpClient) {}

  start(body: { reportType: string; from: string; to: string }): Observable<ReportSnapshot> {
    return this.http.post<ReportSnapshot>(`${base}/reports`, body);
  }

  get(id: number): Observable<ReportSnapshot> {
    return this.http.get<ReportSnapshot>(`${base}/reports/${id}`);
  }

  download(id: number): Observable<Blob> {
    return this.http.get(`${base}/reports/${id}/download`, { responseType: 'blob' });
  }
}

@Injectable({ providedIn: 'root' })
export class AuditLogApi {
  constructor(private readonly http: HttpClient) {}

  list(): Observable<PageResponse<AuditLog>> {
    return this.http.get<PageResponse<AuditLog>>(`${base}/audit-logs`, {
      params: new HttpParams().set('size', '100'),
    });
  }

  search(entityType?: string, actor?: string): Observable<AuditLog[]> {
    let params = new HttpParams();
    if (entityType) params = params.set('entityType', entityType);
    if (actor) params = params.set('actor', actor);
    return this.http.get<AuditLog[]>(`${base}/audit-logs/search`, { params });
  }
}

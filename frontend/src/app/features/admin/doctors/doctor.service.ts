import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { ApiResponse } from '../../../core/models/api-response.model';
import { PageResponse } from '../../../core/models/page-response.model';

export interface Doctor {
  id: number;
  userId?: number;
  fullName: string;
  email?: string;
  specialty: string;
  roomNumber: string;
  maxPatientsPerDay: number;
  active: boolean;
  createdAt?: string;
}

export interface DoctorPayload {
  userId?: number;
  specialty?: string;
  roomNumber?: string;
  maxPatientsPerDay?: number;
  active?: boolean;
}

export interface DoctorQuery {
  page: number;
  size: number;
  search?: string;
  specialty?: string;
  active?: boolean;
}

@Injectable({ providedIn: 'root' })
export class DoctorService {
  private readonly publicUrl = `${environment.apiUrl}/doctors`;
  private readonly adminUrl = `${environment.apiUrl}/admin/doctors`;

  constructor(private http: HttpClient) {}

  getAll(query: DoctorQuery): Observable<ApiResponse<PageResponse<Doctor>>> {
    let params = new HttpParams()
      .set('page', query.page)
      .set('size', query.size);

    if (query.search?.trim()) {
      params = params.set('search', query.search.trim());
    }
    if (query.specialty?.trim()) {
      params = params.set('specialty', query.specialty.trim());
    }
    if (query.active !== undefined) {
      params = params.set('active', query.active);
    }

    return this.http.get<ApiResponse<PageResponse<Doctor>>>(this.publicUrl, { params });
  }

  getById(id: number): Observable<ApiResponse<Doctor>> {
    return this.http.get<ApiResponse<Doctor>>(`${this.publicUrl}/${id}`);
  }

  create(payload: DoctorPayload): Observable<ApiResponse<Doctor>> {
    return this.http.post<ApiResponse<Doctor>>(this.adminUrl, payload);
  }

  update(id: number, payload: DoctorPayload): Observable<ApiResponse<Doctor>> {
    return this.http.put<ApiResponse<Doctor>>(`${this.adminUrl}/${id}`, payload);
  }

  deactivate(id: number): Observable<ApiResponse<Doctor>> {
    return this.update(id, { active: false });
  }

  activate(id: number): Observable<ApiResponse<Doctor>> {
    return this.update(id, { active: true });
  }
}

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../../core/models/api-response.model';
import { PageResponse } from '../../core/models/page-response.model';

export interface Doctor {
  id: number;
  userId: number;
  fullName: string;
  specialty: string;
  roomNumber?: string;
  maxPatientsPerDay: number;
  active: boolean;
  createdAt: string;
}

export interface ScheduleSlot {
  id: number;
  doctorId: number;
  doctorName: string;
  slotDate: string;
  startTime: string;
  endTime: string;
  status: string;
}

export interface Appointment {
  id: number;
  patientId: number;
  patientName: string;
  doctorId: number;
  doctorName: string;
  specialty: string;
  slotId: number;
  appointmentDate: string;
  startTime: string;
  endTime: string;
  status: string;
  symptoms?: string;
  notes?: string;
  createdAt: string;
}

export interface BookAppointmentRequest {
  doctorId: number;
  slotId: number;
  symptoms?: string;
  notes?: string;
}

@Injectable({ providedIn: 'root' })
export class AppointmentApiService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getDoctors(specialty?: string): Observable<ApiResponse<PageResponse<Doctor>>> {
    const params = new URLSearchParams({ page: '0', size: '50' });
    if (specialty) {
      params.set('specialty', specialty);
    }
    return this.http.get<ApiResponse<PageResponse<Doctor>>>(`${this.apiUrl}/doctors?${params.toString()}`);
  }

  getAvailableSlots(doctorId: number, date: string): Observable<ApiResponse<ScheduleSlot[]>> {
    return this.http.get<ApiResponse<ScheduleSlot[]>>(`${this.apiUrl}/doctors/${doctorId}/slots?date=${date}`);
  }

  getMyAppointments(page = 0, size = 10): Observable<ApiResponse<PageResponse<Appointment>>> {
    return this.http.get<ApiResponse<PageResponse<Appointment>>>(`${this.apiUrl}/appointments?page=${page}&size=${size}&sortBy=appointmentDate&sortDir=DESC`);
  }

  bookAppointment(request: BookAppointmentRequest): Observable<ApiResponse<Appointment>> {
    return this.http.post<ApiResponse<Appointment>>(`${this.apiUrl}/appointments`, request);
  }

  cancelAppointment(id: number): Observable<ApiResponse<Appointment>> {
    return this.http.put<ApiResponse<Appointment>>(`${this.apiUrl}/appointments/${id}/cancel`, {});
  }

  getDoctorAppointments(date: string, page = 0, size = 20): Observable<ApiResponse<PageResponse<Appointment>>> {
    return this.http.get<ApiResponse<PageResponse<Appointment>>>(`${this.apiUrl}/doctor/appointments?date=${date}&page=${page}&size=${size}`);
  }
}

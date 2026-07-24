import { CommonModule, DatePipe } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { Appointment, AppointmentApiService } from '../appointment.service';
import { NotificationService } from '../../../core/services/notification.service';

@Component({
  selector: 'app-doctor-appointments',
  standalone: true,
  imports: [CommonModule, DatePipe, ReactiveFormsModule, MatIconModule],
  template: `
    <section class="doctor-page">
      <header>
        <div>
          <p>Doctor workspace</p>
          <h1>Today's appointments</h1>
        </div>
        <label>
          <mat-icon>event</mat-icon>
          <input type="date" [formControl]="dateControl" (change)="loadAppointments()">
        </label>
      </header>

      <div class="list">
        @for (appointment of appointments; track appointment.id) {
          <article>
            <div class="time">{{ appointment.startTime.slice(0, 5) }}</div>
            <div>
              <strong>{{ appointment.patientName }}</strong>
              <span>{{ appointment.symptoms || 'No symptoms provided' }}</span>
            </div>
            <small>{{ appointment.status }}</small>
          </article>
        } @empty {
          <p class="empty">No appointments for this date</p>
        }
      </div>
    </section>
  `,
  styles: [`
    :host { display: block; min-height: 100vh; background: #f2f7fb; color: #16243a; }
    .doctor-page { max-width: 980px; margin: 0 auto; padding: 32px 20px; }
    header { display: flex; align-items: center; justify-content: space-between; gap: 16px; margin-bottom: 24px; }
    h1 { margin: 4px 0 0; font-size: 28px; letter-spacing: 0; }
    p { margin: 0; color: #728097; font-weight: 700; }
    label { display: flex; align-items: center; gap: 10px; height: 44px; padding: 0 14px; background: #fff; border: 1px solid #e2ebf5; border-radius: 8px; }
    input { border: 0; outline: 0; font: inherit; }
    .list { display: grid; gap: 12px; }
    article { display: grid; grid-template-columns: 76px 1fr 116px; gap: 14px; align-items: center; min-height: 76px; padding: 14px 18px; background: #fff; border: 1px solid #e2ebf5; border-radius: 8px; }
    .time { color: #176bdd; font-weight: 900; }
    strong, span { display: block; }
    span { margin-top: 4px; color: #60718a; }
    small { justify-self: end; color: #176bdd; font-weight: 900; }
    .empty { padding: 24px; background: #fff; border-radius: 8px; border: 1px solid #e2ebf5; }
    @media (max-width: 640px) { header { align-items: stretch; flex-direction: column; } article { grid-template-columns: 1fr; } small { justify-self: start; } }
  `],
  providers: [DatePipe]
})
export class DoctorAppointmentsComponent implements OnInit {
  appointments: Appointment[] = [];
  dateControl = this.fb.control(new Date().toISOString().slice(0, 10));

  constructor(
    private fb: FormBuilder,
    private appointmentApi: AppointmentApiService,
    private notification: NotificationService
  ) {}

  ngOnInit(): void {
    this.loadAppointments();
  }

  loadAppointments(): void {
    const date = this.dateControl.value || new Date().toISOString().slice(0, 10);
    this.appointmentApi.getDoctorAppointments(date).subscribe({
      next: res => this.appointments = res.data?.content || [],
      error: err => this.notification.error(err.error?.message || 'Cannot load doctor appointments')
    });
  }
}

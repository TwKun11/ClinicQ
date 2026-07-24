import { CommonModule, DatePipe } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { Appointment, AppointmentApiService, Doctor, ScheduleSlot } from '../appointment.service';
import { NotificationService } from '../../../core/services/notification.service';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-appointment-page',
  standalone: true,
  imports: [CommonModule, DatePipe, RouterLink, ReactiveFormsModule, MatButtonModule, MatIconModule, MatProgressSpinnerModule],
  template: `
    <header class="site-header">
      <a class="brand" routerLink="/appointments" aria-label="YouMed">
        <span class="brand-mark">Y</span>
        <span class="brand-you">You</span><span class="brand-med">Med</span>
      </a>

      <nav class="main-nav" aria-label="Main navigation">
        <a routerLink="/appointments">Đặt khám </a>
        <a href="#">Tư vấn trực tuyến</a>
        <a href="#">Tin Y tế</a>
        <a href="#">Trợ lý y khoa</a>
      </nav>

      <div class="account-box">
        <div class="account-avatar">{{ accountInitial }}</div>
        <div class="account-meta">
          <small>Tài khoản</small>
          <span>{{ authService.getEmail() || authService.getUsername() }}</span>
        </div>
        <button class="logout-button" type="button" (click)="authService.logout()">
          <mat-icon>logout</mat-icon>
          Đăng xuất
        </button>
      </div>
    </header>

    <section class="appointment-shell">
      <aside class="rail" aria-label="Navigation">
        <div class="rail-logo">MedX.</div>
        <button class="rail-item"><mat-icon>home</mat-icon></button>
        <button class="rail-item active"><mat-icon>medical_services</mat-icon></button>
        <button class="rail-item"><mat-icon>favorite_border</mat-icon></button>
        <button class="rail-item"><mat-icon>settings</mat-icon></button>
      </aside>

      <main class="workspace">
        <header class="topbar">
          <label class="search">
            <mat-icon>search</mat-icon>
            <input placeholder="Tìm bác sĩ, chuyên khoa..." [formControl]="searchControl">
          </label>
          <div class="top-actions">
            <button><mat-icon>chat_bubble_outline</mat-icon></button>
            <button><mat-icon>notifications_none</mat-icon></button>
          </div>
        </header>

        <div class="content-grid">
          <section class="booking-flow">
            <h1>Đặt lịch khám</h1>

            <div class="panel category-panel">
              <p>Chọn chuyên khoa</p>
              <div class="chips">
                @for (category of categories; track category.value) {
                  <button class="chip" [class.selected]="selectedSpecialty === category.value" (click)="selectSpecialty(category.value)">
                    <mat-icon>local_hospital</mat-icon>{{ category.label }}
                  </button>
                }
              </div>
            </div>

            <div class="panel doctors-panel">
              <div class="panel-head">
                <p>Chọn bác sĩ</p>
                <span>{{ doctors.length }} bác sĩ</span>
              </div>
              @if (loadingDoctors) {
                <mat-spinner diameter="28"></mat-spinner>
              } @else {
                <div class="doctor-grid">
                  @for (doctor of filteredDoctors; track doctor.id) {
                    <button class="doctor-card" [class.selected]="selectedDoctor?.id === doctor.id" (click)="selectDoctor(doctor)">
                      <div class="avatar">{{ initials(doctor.fullName) }}</div>
                      <div>
                        <strong>{{ doctor.fullName }}</strong>
                        <span>{{ doctor.specialty }}</span>
                        <small><mat-icon>star</mat-icon>{{ doctor.roomNumber || 'ClinicQ' }}</small>
                      </div>
                      <mat-icon>more_vert</mat-icon>
                    </button>
                  }
                </div>
              }
            </div>

            <div class="panel schedule-panel">
              <div class="calendar-board">
                <section class="month-card" aria-label="Appointment calendar">
                  <div class="month-head">
                    <button type="button" (click)="changeMonth(-1)" aria-label="Previous month">
                      <mat-icon>chevron_left</mat-icon>
                    </button>
                    <strong>{{ visibleMonth | date:'MMMM, y' }}</strong>
                    <button type="button" (click)="changeMonth(1)" aria-label="Next month">
                      <mat-icon>chevron_right</mat-icon>
                    </button>
                  </div>
                  <div class="weekday-row">
                    @for (weekday of weekdays; track weekday) {
                      <span>{{ weekday }}</span>
                    }
                  </div>
                  <div class="month-grid">
                    @for (day of monthDays; track day.value) {
                      <button
                        type="button"
                        [class.outside]="!day.inMonth"
                        [class.today]="day.isToday"
                        [class.past]="day.isPast"
                        [class.selected]="dateControl.value === day.value"
                        [disabled]="day.isPast"
                        (click)="selectDate(day.value)">
                        {{ day.day }}
                      </button>
                    }
                  </div>
                </section>

                <section class="time-card" aria-label="Available appointment times">
                  <div class="time-head">
                    <h2>Chọn giờ khám còn trống</h2>
                    <label class="calendar-input">
                      <mat-icon>calendar_month</mat-icon>
                      <input type="date" [min]="todayValue" [formControl]="dateControl" (change)="onDateChanged()">
                    </label>
                  </div>
                  <div class="period-tabs">
                    @for (period of periods; track period.key) {
                      <button
                        type="button"
                        [class.selected]="selectedPeriod === period.key"
                        (click)="selectedPeriod = period.key">
                        <span></span>{{ period.label }}
                      </button>
                    }
                  </div>
                  <p class="available-note">Chỉ hiển thị các khung giờ còn trống.</p>
                  <div class="slot-grid">
                    @if (!selectedDoctor) {
                      <span class="muted">Vui lòng chọn bác sĩ trước</span>
                    } @else if (loadingSlots) {
                      <mat-spinner diameter="28"></mat-spinner>
                    } @else if (filteredSlots.length === 0) {
                      <span class="muted">Không có khung giờ trống trong buổi này</span>
                    } @else {
                      @for (slot of filteredSlots; track slot.id) {
                        <button class="slot" [class.selected]="selectedSlot?.id === slot.id" (click)="selectedSlot = slot">
                          {{ trimTime(slot.startTime) }}
                        </button>
                      }
                    }
                  </div>
                </section>
              </div>

              <form class="book-row" [formGroup]="bookForm" (ngSubmit)="book()">
                <div class="symptom-field">
                  <mat-icon>edit_note</mat-icon>
                  <input placeholder="Triệu chứng hoặc lý do khám" formControlName="symptoms">
                </div>
                <button mat-flat-button type="submit" [disabled]="!selectedDoctor || !selectedSlot || booking">
                  Đặt lịch
                </button>
              </form>
            </div>
          </section>

          <aside class="summary">
            <section class="panel profile-panel">
              <h2>{{ selectedDoctor?.fullName || 'Chọn bác sĩ' }}</h2>
              <p>{{ selectedDoctor?.specialty || 'Chọn bác sĩ để xem các khung giờ khám còn trống.' }}</p>
              <div class="map-preview">
                <mat-icon>location_on</mat-icon>
                <span>{{ selectedDoctor?.roomNumber || 'ClinicQ branch' }}</span>
              </div>
            </section>

            <section class="panel my-list">
              <div class="panel-head">
                <p>Lịch hẹn của tôi</p>
                <button (click)="loadAppointments()"><mat-icon>refresh</mat-icon></button>
              </div>
              @for (appointment of appointments; track appointment.id) {
                <article class="appointment-card">
                  <div>
                    <strong>{{ appointment.doctorName }}</strong>
                    <span>{{ appointment.appointmentDate | date:'dd/MM/yyyy' }} lúc {{ trimTime(appointment.startTime) }}</span>
                    <small>{{ appointment.status }}</small>
                  </div>
                  @if (appointment.status === 'SCHEDULED') {
                    <button class="cancel" (click)="cancel(appointment)">Hủy</button>
                  }
                </article>
              } @empty {
                <p class="muted">Chưa có lịch hẹn</p>
              }
            </section>
          </aside>
        </div>
      </main>
    </section>
  `,
  styles: [`
    :host { display: block; min-height: 100vh; background: #f2f7fb; color: #15223a; }
    .site-header { position: sticky; top: 0; z-index: 20; display: flex; align-items: center; gap: 28px; min-height: 78px; padding: 0 28px; background: rgba(255, 255, 255, 0.96); border-bottom: 1px solid #e9eef5; box-shadow: 0 12px 30px rgba(38, 61, 94, 0.08); backdrop-filter: blur(12px); box-sizing: border-box; }
    .brand { display: inline-flex; align-items: center; min-width: 172px; text-decoration: none; font-size: 34px; font-weight: 900; line-height: 1; letter-spacing: 0; }
    .brand-mark { display: grid; place-items: center; width: 36px; height: 36px; margin-right: 4px; border-radius: 10px; background: linear-gradient(135deg, #155bd4, #237cff); color: #fff; font-size: 26px; font-weight: 900; box-shadow: 0 10px 22px rgba(21, 91, 212, 0.22); }
    .brand-you { color: #155bd4; }
    .brand-med { color: #11b66a; }
    .main-nav { display: flex; align-items: center; justify-content: center; gap: 8px; flex: 1; min-width: 0; padding: 6px; border: 1px solid #edf2f8; border-radius: 999px; background: #f7fbff; font-size: 15px; font-weight: 800; }
    .main-nav a { display: inline-flex; align-items: center; justify-content: center; gap: 4px; min-height: 42px; padding: 0 18px; border-radius: 999px; color: #24324a; text-decoration: none; white-space: nowrap; }
    .main-nav a:first-child { background: #eaf4ff; color: #096fe7; box-shadow: inset 0 0 0 1px #cfe5ff; }
    .chevron { color: #6f9fda; font-size: 16px; line-height: 1; }
    .account-box { display: inline-flex; align-items: center; gap: 12px; min-width: 0; padding: 8px; border: 1px solid #edf2f8; border-radius: 999px; background: #fff; box-shadow: 0 8px 22px rgba(38, 61, 94, 0.08); }
    .account-avatar { display: grid; place-items: center; flex: 0 0 auto; width: 40px; height: 40px; border-radius: 50%; background: linear-gradient(135deg, #e7f6ff, #dff8ea); color: #096fe7; font-size: 15px; font-weight: 900; }
    .account-meta { min-width: 0; display: grid; gap: 1px; }
    .account-meta small { color: #7d8ba1; font-size: 11px; font-weight: 800; }
    .account-box span { max-width: 210px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; color: #172033; font-size: 14px; font-weight: 900; }
    .logout-button { display: inline-flex; align-items: center; justify-content: center; gap: 8px; height: 42px; padding: 0 16px; border: 0; border-radius: 999px; background: #096fe7; color: #fff; font: inherit; font-size: 14px; font-weight: 900; cursor: pointer; box-shadow: 0 10px 20px rgba(9, 111, 231, 0.18); }
    .logout-button mat-icon { width: 20px; height: 20px; font-size: 20px; }
    .appointment-shell { display: grid; grid-template-columns: 76px 1fr; min-height: calc(100vh - 82px); padding: 18px; box-sizing: border-box; gap: 18px; }
    .rail { display: flex; flex-direction: column; align-items: center; gap: 18px; padding: 22px 10px; background: #101820; border-radius: 8px; color: #fff; }
    .rail-logo { color: #d8ff3e; font-weight: 800; font-size: 12px; margin-bottom: 28px; }
    .rail-item { width: 42px; height: 42px; display: grid; place-items: center; border: 0; border-radius: 8px; background: transparent; color: #8f9aaa; cursor: pointer; }
    .rail-item.active { background: #4f94ff; color: #fff; }
    .workspace { min-width: 0; }
    .topbar { display: flex; align-items: center; justify-content: space-between; gap: 24px; margin-bottom: 18px; }
    .search { display: flex; align-items: center; gap: 10px; width: min(100%, 520px); height: 52px; padding: 0 18px; background: #fff; border: 1px solid #e6eef7; border-radius: 8px; }
    .search input, .book-row input, .schedule-panel input[type=date] { border: 0; outline: 0; font: inherit; background: transparent; width: 100%; }
    .top-actions { display: flex; gap: 12px; }
    .top-actions button, .panel-head button { width: 42px; height: 42px; border: 0; border-radius: 50%; background: #fff; color: #40506a; cursor: pointer; }
    .content-grid { display: grid; grid-template-columns: minmax(0, 1.4fr) minmax(280px, .8fr); gap: 18px; align-items: start; }
    h1 { margin: 0 0 14px; font-size: 24px; letter-spacing: 0; }
    .panel { background: #fff; border: 1px solid #e6eef7; border-radius: 8px; padding: 18px; margin-bottom: 14px; }
    .panel p { margin: 0 0 12px; color: #758197; font-size: 14px; }
    .panel-head { display: flex; align-items: center; justify-content: space-between; gap: 12px; margin-bottom: 12px; }
    .panel-head p { margin: 0; }
    .panel-head strong { display: block; margin-top: 6px; color: #172033; font-size: 18px; }
    .schedule-panel { padding: 24px; }
    .calendar-board { display: grid; grid-template-columns: 260px minmax(0, 1fr); gap: 34px; align-items: start; }
    .month-card, .time-card { min-width: 0; }
    .month-card { padding: 2px 4px 6px; }
    .time-card { padding: 2px 0 6px; }
    .month-head { display: grid; grid-template-columns: 34px 1fr 34px; align-items: center; gap: 10px; margin-bottom: 22px; }
    .month-head strong { text-align: center; color: #172033; font-size: 15px; }
    .month-head button { display: grid; place-items: center; width: 34px; height: 34px; border: 0; border-radius: 50%; background: #f5f8fc; color: #172033; cursor: pointer; }
    .month-head mat-icon { width: 20px; height: 20px; font-size: 20px; }
    .weekday-row, .month-grid { display: grid; grid-template-columns: repeat(7, 1fr); gap: 10px; }
    .weekday-row { margin-bottom: 12px; }
    .weekday-row span { color: #172033; font-size: 11px; font-weight: 900; text-align: center; }
    .month-grid button { display: grid; place-items: center; width: 32px; height: 32px; border: 0; border-radius: 50%; background: transparent; color: #526174; font: inherit; font-size: 13px; cursor: pointer; }
    .month-grid button.outside { color: #95a0af; }
    .month-grid button.past { color: #c5ccd6; cursor: not-allowed; text-decoration: line-through; }
    .month-grid button.past:hover { background: transparent; }
    .month-grid button.today { background: #e9fbff; color: #2597a8; font-weight: 800; }
    .month-grid button.selected { background: #0c6674; color: #fff; font-weight: 900; box-shadow: 0 10px 20px rgba(12, 102, 116, 0.18); }
    .time-head { display: flex; align-items: center; justify-content: space-between; gap: 16px; margin-bottom: 18px; }
    .time-head h2 { margin: 0; color: #172033; font-size: 15px; font-weight: 900; letter-spacing: 0; }
    .calendar-input { display: inline-flex; align-items: center; justify-content: center; gap: 8px; min-width: 156px; height: 38px; padding: 0 10px; border: 1px solid #dfe8f4; border-radius: 8px; background: #f8fbff; color: #2a4361; }
    .calendar-input mat-icon { width: 18px; height: 18px; font-size: 18px; }
    .calendar-input input { border: 0; outline: 0; background: transparent; font: inherit; font-size: 12px; font-weight: 800; color: #172033; }
    .period-tabs { display: grid; grid-template-columns: repeat(3, minmax(130px, 1fr)); gap: 14px; margin-bottom: 16px; }
    .period-tabs button { display: inline-flex; align-items: center; justify-content: center; gap: 10px; min-height: 54px; padding: 0 18px; border: 1px solid #e5e9ef; border-radius: 8px; background: #fff; color: #526174; font: inherit; font-size: 13px; font-weight: 800; cursor: pointer; }
    .period-tabs span { width: 12px; height: 12px; border: 2px solid #e1e5eb; border-radius: 50%; box-sizing: border-box; }
    .period-tabs button.selected { border-color: #8edce8; background: #effcff; color: #156f7d; }
    .period-tabs button.selected span { border: 3px solid #2597a8; }
    .available-note { margin: 0 0 18px; color: #526174; font-size: 12px; font-weight: 700; }
    .slot-grid { display: grid; grid-template-columns: repeat(4, minmax(120px, 1fr)); gap: 14px; min-height: 108px; align-items: start; }
    .slot-grid .muted, .slot-grid mat-spinner { grid-column: 1 / -1; }
    .chips { display: flex; flex-wrap: wrap; gap: 10px; min-height: 42px; align-items: center; }
    .chip, .slot { min-height: 34px; padding: 0 13px; border: 1px solid #e8eef6; border-radius: 999px; background: #f7f9fc; color: #263756; cursor: pointer; font-weight: 700; font-size: 12px; }
    .slot { min-height: 48px; border-radius: 8px; background: #fff; font-size: 13px; }
    .chip { display: inline-flex; align-items: center; gap: 6px; }
    .chip mat-icon { width: 16px; height: 16px; font-size: 16px; color: #4f94ff; }
    .chip.selected, .slot.selected { border-color: #4f94ff; background: #eef5ff; color: #1d67d3; }
    .doctor-grid { display: grid; grid-template-columns: repeat(2, minmax(220px, 1fr)); gap: 12px; }
    .doctor-card { display: grid; grid-template-columns: 54px 1fr 22px; gap: 12px; align-items: center; min-height: 82px; padding: 10px; border: 1px solid #edf2f8; border-radius: 8px; background: #fff; text-align: left; cursor: pointer; }
    .doctor-card.selected { border-color: #4f94ff; background: #eef5ff; }
    .avatar { display: grid; place-items: center; width: 52px; height: 52px; border-radius: 8px; background: linear-gradient(135deg, #e9f1ff, #d7f7e8); color: #1d67d3; font-weight: 900; }
    .doctor-card strong, .appointment-card strong { display: block; font-size: 14px; }
    .doctor-card span, .appointment-card span { display: block; color: #607087; font-size: 12px; margin-top: 3px; }
    .doctor-card small { display: inline-flex; align-items: center; gap: 4px; color: #1d67d3; font-size: 11px; margin-top: 6px; }
    .doctor-card small mat-icon { width: 13px; height: 13px; font-size: 13px; }
    .book-row { display: grid; grid-template-columns: 1fr 156px; gap: 14px; margin-top: 24px; padding: 14px; border: 1px solid #dff19c; border-radius: 8px; background: linear-gradient(90deg, #fbffe8, #f6ffd2); }
    .symptom-field { display: flex; align-items: center; gap: 10px; min-height: 52px; padding: 0 16px; border-radius: 8px; background: #fff; color: #7b8798; }
    .symptom-field input { border: 0; outline: 0; width: 100%; font: inherit; background: transparent; }
    .book-row button { border-radius: 8px; background: #d9f23f; color: #101820; font-weight: 900; }
    .profile-panel h2 { margin: 0 0 8px; font-size: 18px; letter-spacing: 0; }
    .profile-panel p { line-height: 1.5; }
    .map-preview { display: flex; align-items: center; gap: 10px; min-height: 112px; padding: 16px; border-radius: 8px; background: #edf7ff; color: #315475; }
    .appointment-card { display: flex; align-items: center; justify-content: space-between; gap: 12px; padding: 12px 0; border-top: 1px solid #eef2f7; }
    .appointment-card small { display: inline-block; margin-top: 7px; color: #1d67d3; font-weight: 800; }
    .cancel { height: 32px; border: 1px solid #ffb9b9; border-radius: 8px; background: #fff5f5; color: #bd2222; cursor: pointer; }
    .muted { color: #8490a3; font-size: 14px; }
    @media (max-width: 1180px) { .brand { min-width: auto; } .main-nav { justify-content: flex-start; overflow-x: auto; } .main-nav a { padding: 0 14px; } .account-meta { display: none; } .calendar-board { grid-template-columns: 1fr; } }
    @media (max-width: 980px) { .site-header { height: auto; min-height: 72px; flex-wrap: wrap; padding: 14px 18px; } .main-nav { order: 3; width: 100%; } .account-box { margin-left: auto; } .content-grid { grid-template-columns: 1fr; } .doctor-grid { grid-template-columns: 1fr; } }
    @media (max-width: 680px) { .appointment-shell { grid-template-columns: 1fr; padding: 12px; } .rail { flex-direction: row; justify-content: space-between; } .rail-logo { margin: 0; } .topbar, .panel-head, .time-head { flex-direction: column; align-items: stretch; } .calendar-input { width: 100%; box-sizing: border-box; } .period-tabs, .slot-grid { grid-template-columns: 1fr; } .book-row { grid-template-columns: 1fr; } .brand { font-size: 29px; } .brand-mark { width: 30px; height: 30px; font-size: 22px; } .account-box { gap: 8px; padding: 6px; } .account-avatar { width: 36px; height: 36px; } .logout-button { width: 42px; padding: 0; } .logout-button { font-size: 0; } }
  `],
  providers: [DatePipe]
})
export class AppointmentPageComponent implements OnInit {
  categories = [
    { label: 'Nhi khoa', value: 'Pediatrics' },
    { label: 'Chấn thương', value: 'Traumatology' },
    { label: 'Tim mạch', value: 'Cardiology' },
    { label: 'Nội tiết', value: 'Endocrinology' },
    { label: 'Mắt', value: 'Ophthalmology' },
    { label: 'Tâm lý', value: 'Psychology' },
    { label: 'Hô hấp', value: 'Pulmonology' },
    { label: 'Ung bướu', value: 'Oncology' }
  ];
  selectedSpecialty = '';
  doctors: Doctor[] = [];
  slots: ScheduleSlot[] = [];
  appointments: Appointment[] = [];
  selectedDoctor?: Doctor;
  selectedSlot?: ScheduleSlot;
  weekdays = ['MON', 'TUE', 'WED', 'THU', 'FRI', 'SAT', 'SUN'];
  periods: Array<{ key: 'morning' | 'afternoon' | 'evening'; label: string }> = [
    { key: 'morning', label: 'Buổi sáng' },
    { key: 'afternoon', label: 'Buổi chiều' },
    { key: 'evening', label: 'Buổi tối' }
  ];
  selectedPeriod: 'morning' | 'afternoon' | 'evening' = 'morning';
  todayValue = this.today();
  visibleMonth = new Date(`${this.today()}T00:00:00`);
  monthDays = this.buildMonthDays(this.visibleMonth);
  loadingDoctors = false;
  loadingSlots = false;
  booking = false;
  dateControl = this.fb.control(this.today());
  searchControl = this.fb.control('');
  bookForm = this.fb.group({ symptoms: [''], notes: [''] });

  constructor(
    private fb: FormBuilder,
    private appointmentApi: AppointmentApiService,
    private notification: NotificationService,
    public authService: AuthService
  ) {}

  get filteredDoctors(): Doctor[] {
    const term = (this.searchControl.value || '').toLowerCase().trim();
    return this.doctors.filter(doctor => {
      const matchesTerm = !term || `${doctor.fullName} ${doctor.specialty}`.toLowerCase().includes(term);
      const matchesSpecialty = !this.selectedSpecialty || doctor.specialty.toLowerCase() === this.selectedSpecialty.toLowerCase();
      return matchesTerm && matchesSpecialty && doctor.active;
    });
  }

  get filteredSlots(): ScheduleSlot[] {
    return this.slots.filter(slot => {
      const hour = Number(slot.startTime.slice(0, 2));
      if (this.selectedPeriod === 'morning') {
        return hour < 12;
      }
      if (this.selectedPeriod === 'afternoon') {
        return hour >= 12 && hour < 17;
      }
      return hour >= 17;
    });
  }

  get accountInitial(): string {
    const identity = this.authService.getEmail() || this.authService.getUsername() || 'U';
    return identity.trim().charAt(0).toUpperCase();
  }

  ngOnInit(): void {
    this.loadDoctors();
    this.loadAppointments();
  }

  selectSpecialty(specialty: string): void {
    this.selectedSpecialty = this.selectedSpecialty === specialty ? '' : specialty;
  }

  selectDoctor(doctor: Doctor): void {
    this.selectedDoctor = doctor;
    this.selectedSlot = undefined;
    this.loadSlots();
  }

  selectDate(value: string): void {
    if (this.isPastDate(value)) {
      this.notification.info('Không thể chọn ngày trong quá khứ');
      return;
    }
    this.dateControl.setValue(value);
    this.visibleMonth = new Date(`${value}T00:00:00`);
    this.monthDays = this.buildMonthDays(this.visibleMonth);
    this.loadSlots();
  }

  onDateChanged(): void {
    let value = this.dateControl.value || this.todayValue;
    if (this.isPastDate(value)) {
      this.notification.info('Không thể chọn ngày trong quá khứ');
      value = this.todayValue;
      this.dateControl.setValue(value);
    }
    this.visibleMonth = new Date(`${value}T00:00:00`);
    this.monthDays = this.buildMonthDays(this.visibleMonth);
    this.loadSlots();
  }

  changeMonth(delta: number): void {
    const next = new Date(this.visibleMonth);
    next.setMonth(next.getMonth() + delta);
    this.visibleMonth = next;
    this.monthDays = this.buildMonthDays(next);
  }

  loadDoctors(): void {
    this.loadingDoctors = true;
    this.appointmentApi.getDoctors().subscribe({
      next: res => {
        this.doctors = res.data?.content || [];
        this.loadingDoctors = false;
      },
      error: err => {
        this.loadingDoctors = false;
        this.notification.error(err.error?.message || 'Không tải được danh sách bác sĩ');
      }
    });
  }

  loadSlots(): void {
    if (!this.selectedDoctor || !this.dateControl.value) {
      return;
    }
    this.loadingSlots = true;
    this.appointmentApi.getAvailableSlots(this.selectedDoctor.id, this.dateControl.value).subscribe({
      next: res => {
        this.slots = res.data || [];
        this.selectedSlot = undefined;
        this.loadingSlots = false;
      },
      error: err => {
        this.loadingSlots = false;
        this.notification.error(err.error?.message || 'Không tải được khung giờ khám');
      }
    });
  }

  loadAppointments(): void {
    this.appointmentApi.getMyAppointments().subscribe({
      next: res => this.appointments = res.data?.content || [],
      error: err => this.notification.error(err.error?.message || 'Không tải được lịch hẹn')
    });
  }

  book(): void {
    if (!this.selectedDoctor || !this.selectedSlot) {
      return;
    }
    this.booking = true;
    this.appointmentApi.bookAppointment({
      doctorId: this.selectedDoctor.id,
      slotId: this.selectedSlot.id,
      symptoms: this.bookForm.value.symptoms || undefined,
      notes: this.bookForm.value.notes || undefined
    }).subscribe({
      next: () => {
        this.notification.success('Đặt lịch thành công');
        this.booking = false;
        this.bookForm.reset();
        this.loadSlots();
        this.loadAppointments();
      },
      error: err => {
        this.booking = false;
        this.notification.error(err.error?.message || 'Đặt lịch thất bại');
      }
    });
  }

  cancel(appointment: Appointment): void {
    this.appointmentApi.cancelAppointment(appointment.id).subscribe({
      next: () => {
        this.notification.success('Đã hủy lịch hẹn');
        this.loadAppointments();
        this.loadSlots();
      },
      error: err => this.notification.error(err.error?.message || 'Hủy lịch thất bại')
    });
  }

  initials(name: string): string {
    return name.split(' ').filter(Boolean).slice(0, 2).map(part => part[0]).join('').toUpperCase();
  }

  trimTime(value: string): string {
    return value?.slice(0, 5) || '';
  }

  private today(): string {
    const now = new Date();
    const month = `${now.getMonth() + 1}`.padStart(2, '0');
    const day = `${now.getDate()}`.padStart(2, '0');
    return `${now.getFullYear()}-${month}-${day}`;
  }

  private isPastDate(value: string): boolean {
    return value < this.todayValue;
  }

  private buildMonthDays(monthDate: Date): Array<{ value: string; day: number; inMonth: boolean; isToday: boolean; isPast: boolean }> {
    const first = new Date(monthDate.getFullYear(), monthDate.getMonth(), 1);
    const start = new Date(first);
    const mondayOffset = (first.getDay() + 6) % 7;
    start.setDate(first.getDate() - mondayOffset);
    const today = this.today();
    return Array.from({ length: 42 }, (_, index) => {
      const date = new Date(start);
      date.setDate(start.getDate() + index);
      const value = date.toISOString().slice(0, 10);
      return {
        value,
        day: date.getDate(),
        inMonth: date.getMonth() === monthDate.getMonth(),
        isToday: value === today,
        isPast: value < today
      };
    });
  }
}


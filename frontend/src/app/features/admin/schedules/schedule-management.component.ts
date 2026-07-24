import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';

type SlotStatus = 'AVAILABLE' | 'BOOKED' | 'BLOCKED';

interface ScheduleSlot {
  time: string;
  status: SlotStatus;
  patient?: string;
}

interface Session {
  title: string;
  icon: string;
  slots: ScheduleSlot[];
}

interface ScheduleDoctor {
  id: number;
  code: string;
  name: string;
  specialty: string;
  room: string;
  initials: string;
  sessions: Session[];
}

@Component({
  selector: 'app-schedule-management',
  standalone: true,
  imports: [CommonModule, FormsModule, MatIconModule],
  template: `
    <section class="schedule-page">
      <header class="schedule-header">
        <div>
          <span>Quản trị lịch khám</span>
          <h1>Quản lý lịch khám</h1>
          <p>Chọn bác sĩ để xem, tạo và điều phối các khung giờ khám trong ngày.</p>
        </div>
        <button class="primary-button" type="button" [disabled]="!selectedDoctor" (click)="drawerOpen = true">
          <mat-icon>add_circle</mat-icon>Tạo khung giờ
        </button>
      </header>

      <section class="doctor-selector">
        <div class="selector-filters">
          <label>
            <span>Tìm bác sĩ</span>
            <input type="search" [(ngModel)]="doctorSearch" placeholder="Tên, mã bác sĩ hoặc phòng..." />
          </label>
          <label>
            <span>Chuyên khoa</span>
            <select [(ngModel)]="specialtyFilter">
              <option value="">Tất cả chuyên khoa</option>
              @for (specialty of specialties; track specialty) {
                <option [value]="specialty">{{ specialty }}</option>
              }
            </select>
          </label>
        </div>

        <div class="doctor-list">
          @for (doctor of filteredDoctors; track doctor.id) {
            <button type="button" class="doctor-card" [class.selected]="selectedDoctor.id === doctor.id" (click)="selectDoctor(doctor)">
              <span class="portrait">{{ doctor.initials }}</span>
              <span>
                <strong>{{ doctor.name }}</strong>
                <small>{{ doctor.specialty }} · {{ doctor.room }} · {{ doctor.code }}</small>
              </span>
              <mat-icon>chevron_right</mat-icon>
            </button>
          } @empty {
            <div class="empty-doctors">Không tìm thấy bác sĩ phù hợp.</div>
          }
        </div>
      </section>

      @if (selectedDoctor) {
        <header class="doctor-header">
          <div class="doctor-info">
            <span class="portrait">{{ selectedDoctor.initials }}</span>
            <div>
              <h1>{{ selectedDoctor.name }}</h1>
              <div class="meta-row">
                <span class="pill">{{ selectedDoctor.specialty }}</span>
                <span><mat-icon>meeting_room</mat-icon>{{ selectedDoctor.room }}</span>
                <span>Mã BS: {{ selectedDoctor.code }}</span>
              </div>
            </div>
          </div>
        </header>
      }

      <div class="toolbar">
        <div class="date-nav">
          <button type="button"><mat-icon>chevron_left</mat-icon></button>
          <button type="button">Hôm nay</button>
          <button type="button"><mat-icon>chevron_right</mat-icon></button>
          <span><mat-icon>calendar_today</mat-icon>Thứ Ba, 24 Tháng 5, 2024</span>
        </div>
        <div class="toolbar-right">
          <div class="legend">
            <span><i class="available"></i>Còn trống</span>
            <span><i class="booked"></i>Đã đặt</span>
            <span><i class="blocked"></i>Đã khóa</span>
          </div>
          <div class="view-toggle">
            <button type="button" [class.active]="viewMode === 'day'" (click)="viewMode = 'day'">Ngày</button>
            <button type="button" [class.active]="viewMode === 'week'" (click)="viewMode = 'week'">Tuần</button>
          </div>
        </div>
      </div>

      <div class="stats-grid">
        <article><span>Tổng khung giờ</span><strong>{{ totalSlots }}</strong><small>lịch/ngày</small></article>
        <article><span>Còn trống</span><strong class="primary-text">{{ availableSlots }}</strong><small>có thể đặt</small></article>
        <article><span>Đã đặt</span><strong class="secondary-text">{{ bookedSlots }}</strong><small>lịch mới</small></article>
        <article><span>Đã khóa</span><strong class="error-text">{{ blockedSlots }}</strong><small>bảo trì/nghỉ</small></article>
      </div>

      <section class="timeline-panel">
        <div class="session-grid">
          @for (session of sessions; track session.title) {
            <article class="session-card" [class.wide]="session.title === 'Buổi chiều'">
              <h2><mat-icon>{{ session.icon }}</mat-icon>{{ session.title }}</h2>
              <div class="slot-grid">
                @for (slot of session.slots; track slot.time) {
                  <button class="slot" type="button" [class]="slot.status.toLowerCase()" (click)="selectSlot(slot)">
                    <span>{{ slot.time }}</span>
                    <b>{{ statusLabel(slot.status) }}</b>
                    @if (slot.patient) {
                      <small>{{ slot.patient }}</small>
                    }
                    @if (slot.status === 'BLOCKED') {
                      <mat-icon>lock</mat-icon>
                    }
                  </button>
                }
              </div>
            </article>
          }
        </div>
        <footer><mat-icon>info</mat-icon>Chọn bác sĩ phía trên để chuyển lịch đang quản lý.</footer>
      </section>

      @if (drawerOpen) {
        <div class="schedule-drawer-shell">
          <button class="backdrop" type="button" (click)="drawerOpen = false" aria-label="Đóng"></button>
          <aside class="drawer">
            <header>
              <div>
                <h2>Tạo khung giờ khám</h2>
                <p>{{ selectedDoctor.name }} · {{ selectedDoctor.room }}</p>
              </div>
              <button type="button" (click)="drawerOpen = false"><mat-icon>close</mat-icon></button>
            </header>

            <form>
              <label>Khoảng thời gian áp dụng</label>
              <div class="two-cols">
                <input type="date" [(ngModel)]="slotForm.startDate" name="startDate" />
                <input type="date" [(ngModel)]="slotForm.endDate" name="endDate" />
              </div>

              <label>Các ngày trong tuần</label>
              <div class="weekday-grid">
                @for (day of weekdays; track day.value) {
                  <label>
                    <input type="checkbox" [(ngModel)]="day.checked" [name]="day.value" />
                    <span>{{ day.label }}</span>
                  </label>
                }
              </div>

              <div class="two-cols">
                <label>Bắt đầu<input type="time" [(ngModel)]="slotForm.startTime" name="startTime" /></label>
                <label>Kết thúc<input type="time" [(ngModel)]="slotForm.endTime" name="endTime" /></label>
              </div>

              <label>Thời lượng mỗi ca</label>
              <select [(ngModel)]="slotForm.duration" name="duration">
                <option [ngValue]="15">15 phút</option>
                <option [ngValue]="30">30 phút</option>
                <option [ngValue]="45">45 phút</option>
                <option [ngValue]="60">60 phút</option>
              </select>

              <label>Thời gian nghỉ</label>
              <div class="break-row">
                <input type="time" [(ngModel)]="slotForm.breakStart" name="breakStart" />
                <span>đến</span>
                <input type="time" [(ngModel)]="slotForm.breakEnd" name="breakEnd" />
                <button type="button"><mat-icon>delete</mat-icon></button>
              </div>
            </form>

            <footer>
              <button type="button" (click)="drawerOpen = false">Hủy bỏ</button>
              <button type="button" class="confirm" (click)="drawerOpen = false">Xác nhận tạo</button>
            </footer>
          </aside>
        </div>
      }
    </section>
  `,
  styles: []
})
export class ScheduleManagementComponent {
  drawerOpen = false;
  viewMode: 'day' | 'week' = 'day';
  doctorSearch = '';
  specialtyFilter = '';

  readonly doctors: ScheduleDoctor[] = [
    { id: 1, code: 'QDOC-0012', name: 'BS. Nguyễn Minh Anh', specialty: 'Nội tổng quát', room: 'Phòng 101', initials: 'NA', sessions: this.buildSessions(['Trần Anh Tuấn', 'Đỗ Minh Khang']) },
    { id: 2, code: 'QDOC-0021', name: 'BS. Trần Hoài Phương', specialty: 'Nhi khoa', room: 'Phòng 204', initials: 'TP', sessions: this.buildSessions(['Lê Gia Bảo', 'Nguyễn Khánh Linh']) },
    { id: 3, code: 'QDOC-0033', name: 'BS. Phạm Thanh Mai', specialty: 'Sản phụ khoa', room: 'Phòng 305', initials: 'PM', sessions: this.buildSessions(['Hoàng Thị Mai', 'Võ Anh Thư']) },
    { id: 4, code: 'QDOC-0040', name: 'BS. Lê Quốc Huy', specialty: 'Da liễu', room: 'Phòng 112', initials: 'LH', sessions: this.buildSessions(['Nguyễn Nhật Nam']) }
  ];

  selectedDoctor: ScheduleDoctor = this.doctors[0];

  readonly weekdays = [
    { label: 'Thứ 2', value: 'mon', checked: true },
    { label: 'Thứ 3', value: 'tue', checked: true },
    { label: 'Thứ 4', value: 'wed', checked: true },
    { label: 'Thứ 5', value: 'thu', checked: true },
    { label: 'Thứ 6', value: 'fri', checked: true },
    { label: 'Thứ 7', value: 'sat', checked: false },
    { label: 'CN', value: 'sun', checked: false }
  ];

  slotForm = { startDate: '', endDate: '', startTime: '08:00', endTime: '17:00', duration: 30, breakStart: '12:00', breakEnd: '13:30' };

  get specialties(): string[] {
    return Array.from(new Set(this.doctors.map(doctor => doctor.specialty)));
  }

  get filteredDoctors(): ScheduleDoctor[] {
    const term = this.doctorSearch.trim().toLowerCase();
    return this.doctors.filter(doctor => {
      const matchesSearch = !term || `${doctor.name} ${doctor.code} ${doctor.room}`.toLowerCase().includes(term);
      const matchesSpecialty = !this.specialtyFilter || doctor.specialty === this.specialtyFilter;
      return matchesSearch && matchesSpecialty;
    });
  }

  get sessions(): Session[] { return this.selectedDoctor?.sessions || []; }
  get allSlots(): ScheduleSlot[] { return this.sessions.flatMap(session => session.slots); }
  get totalSlots(): number { return this.allSlots.length; }
  get availableSlots(): number { return this.count('AVAILABLE'); }
  get bookedSlots(): number { return this.count('BOOKED'); }
  get blockedSlots(): number { return this.count('BLOCKED'); }

  selectDoctor(doctor: ScheduleDoctor): void {
    this.selectedDoctor = doctor;
  }

  statusLabel(status: SlotStatus): string {
    return status === 'AVAILABLE' ? 'Còn trống' : status === 'BOOKED' ? 'Đã đặt' : 'Đã khóa';
  }

  selectSlot(slot: ScheduleSlot): void {
    if (slot.status === 'AVAILABLE') this.drawerOpen = true;
  }

  private count(status: SlotStatus): number {
    return this.allSlots.filter(slot => slot.status === status).length;
  }

  private buildSessions(patients: string[]): Session[] {
    return [
      { title: 'Buổi sáng', icon: 'wb_sunny', slots: [
        { time: '08:00 - 08:30', status: 'AVAILABLE' },
        { time: '08:30 - 09:00', status: 'BOOKED', patient: patients[0] },
        { time: '09:00 - 09:30', status: 'AVAILABLE' },
        { time: '09:30 - 10:00', status: 'BLOCKED' },
        { time: '10:00 - 10:30', status: 'AVAILABLE' },
        { time: '10:30 - 11:00', status: 'AVAILABLE' }
      ] },
      { title: 'Buổi trưa', icon: 'light_mode', slots: [
        { time: '11:00 - 11:30', status: 'AVAILABLE' },
        { time: '11:30 - 12:00', status: 'AVAILABLE' },
        { time: '13:30 - 14:00', status: 'AVAILABLE' }
      ] },
      { title: 'Buổi chiều', icon: 'bedtime', slots: [
        { time: '14:00 - 14:30', status: 'BOOKED', patient: patients[1] || patients[0] },
        { time: '14:30 - 15:00', status: 'BOOKED' },
        { time: '15:00 - 15:30', status: 'BOOKED' },
        { time: '15:30 - 16:00', status: 'AVAILABLE' },
        { time: '16:00 - 16:30', status: 'AVAILABLE' },
        { time: '16:30 - 17:00', status: 'BLOCKED' }
      ] }
    ];
  }
}

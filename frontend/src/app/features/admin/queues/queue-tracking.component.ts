import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';

type QueueStatus = 'WAITING' | 'IN_PROGRESS' | 'COMPLETED';

interface QueuePatient {
  id: number;
  queueNo: string;
  patientCode: string;
  patientName: string;
  initials: string;
  age: number;
  gender: string;
  doctor: string;
  room: string;
  specialty: string;
  service: string;
  appointmentType: string;
  checkInTime: string;
  status: QueueStatus;
  notes: string;
}

@Component({
  selector: 'app-queue-tracking',
  standalone: true,
  imports: [CommonModule, FormsModule, MatIconModule],
  template: `
    <section class="queue-page">
      <header class="queue-topbar">
        <label class="queue-search">
          <mat-icon>search</mat-icon>
          <input [(ngModel)]="searchTerm" type="search" placeholder="Tìm kiếm bệnh nhân (Tên, SĐT, Mã BN)..." />
        </label>
        <div class="queue-topbar-actions">
          <span class="live-chip"><i></i>Real-time Active</span>
          <button class="icon-button" type="button" aria-label="Thông báo"><mat-icon>notifications</mat-icon></button>
          <div class="admin-profile">
            <div>
              <strong>Admin Clinic</strong>
              <span>Administrator</span>
            </div>
            <span class="avatar">AC</span>
          </div>
        </div>
      </header>

      <div class="queue-content">
        <section class="queue-heading">
          <div>
            <h1>Theo dõi hàng đợi</h1>
            <p>Giám sát trạng thái bệnh nhân theo thời gian thực tại các phòng khám.</p>
          </div>
          <div class="queue-filters">
            <select [(ngModel)]="specialtyFilter">
              <option value="">Tất cả chuyên khoa</option>
              @for (specialty of specialties; track specialty) {
                <option [value]="specialty">{{ specialty }}</option>
              }
            </select>
            <select [(ngModel)]="doctorFilter">
              <option value="">Tất cả bác sĩ</option>
              @for (doctor of doctors; track doctor) {
                <option [value]="doctor">{{ doctor }}</option>
              }
            </select>
            <input [(ngModel)]="selectedDate" type="date" />
            <button type="button" (click)="applyFilters()">
              <mat-icon>filter_list</mat-icon>
              Lọc dữ liệu
            </button>
          </div>
        </section>

        <section class="queue-kpis">
          <article>
            <span class="kpi-icon primary"><mat-icon>groups</mat-icon></span>
            <div><p>Tổng bệnh nhân</p><strong>{{ filteredPatients.length }}</strong></div>
          </article>
          <article>
            <span class="kpi-icon warning"><mat-icon>hourglass_empty</mat-icon></span>
            <div><p>Đang chờ</p><strong>{{ countStatus('WAITING') }}</strong></div>
          </article>
          <article>
            <span class="kpi-icon info"><mat-icon>medical_information</mat-icon></span>
            <div><p>Đang khám</p><strong>{{ countStatus('IN_PROGRESS') }}</strong></div>
          </article>
          <article>
            <span class="kpi-icon success"><mat-icon>check_circle</mat-icon></span>
            <div><p>Hoàn thành</p><strong>{{ countStatus('COMPLETED') }}</strong></div>
          </article>
        </section>

        <section class="queue-table-card">
          <div class="queue-table-wrap">
            <table>
              <thead>
                <tr>
                  <th>STT</th>
                  <th>Bệnh nhân</th>
                  <th>Bác sĩ / Phòng</th>
                  <th>Check-in</th>
                  <th>Trạng thái</th>
                  <th class="right">Thao tác</th>
                </tr>
              </thead>
              <tbody>
                @for (patient of pagedPatients; track patient.id) {
                  <tr (click)="openDetail(patient)">
                    <td><strong>{{ patient.queueNo }}</strong></td>
                    <td>
                      <div class="patient-cell">
                        <span class="patient-avatar" [class.completed]="patient.status === 'COMPLETED'">{{ patient.initials }}</span>
                        <div>
                          <strong>{{ patient.patientName }}</strong>
                          <small>{{ patient.patientCode }}</small>
                        </div>
                      </div>
                    </td>
                    <td>
                      <strong>{{ patient.doctor }}</strong>
                      <small>{{ patient.room }} - {{ patient.specialty }}</small>
                    </td>
                    <td>{{ patient.checkInTime }}</td>
                    <td>
                      <span class="status-pill" [class.waiting]="patient.status === 'WAITING'" [class.in-progress]="patient.status === 'IN_PROGRESS'" [class.completed]="patient.status === 'COMPLETED'">
                        <i></i>{{ statusLabel(patient.status) }}
                      </span>
                    </td>
                    <td class="right">
                      <button class="table-action" type="button" aria-label="Xem chi tiết" (click)="openDetail(patient); $event.stopPropagation()">
                        <mat-icon>visibility</mat-icon>
                      </button>
                    </td>
                  </tr>
                } @empty {
                  <tr>
                    <td colspan="6" class="empty-state">Không có bệnh nhân phù hợp với bộ lọc hiện tại.</td>
                  </tr>
                }
              </tbody>
            </table>
          </div>

          <footer class="queue-pagination">
            <p>Hiển thị {{ pagedPatients.length }}/{{ filteredPatients.length }} bệnh nhân</p>
            <div>
              <button type="button" [disabled]="page === 1" (click)="page = page - 1"><mat-icon>chevron_left</mat-icon></button>
              @for (pageNumber of pageNumbers; track pageNumber) {
                <button type="button" [class.active]="page === pageNumber" (click)="page = pageNumber">{{ pageNumber }}</button>
              }
              <button type="button" [disabled]="page === totalPages" (click)="page = page + 1"><mat-icon>chevron_right</mat-icon></button>
            </div>
          </footer>
        </section>
      </div>

      @if (selectedPatient) {
        <div class="queue-drawer-shell">
          <button class="backdrop" type="button" aria-label="Đóng chi tiết" (click)="closeDetail()"></button>
          <aside class="drawer">
            <header>
              <h2>Chi tiết hàng đợi</h2>
              <button class="icon-button" type="button" aria-label="Đóng" (click)="closeDetail()"><mat-icon>close</mat-icon></button>
            </header>

            <div class="drawer-body">
              <section class="detail-profile">
                <span class="profile-avatar">{{ selectedPatient.initials }}</span>
                <div>
                  <h3>{{ selectedPatient.patientName }}</h3>
                  <p>{{ selectedPatient.patientCode }} | {{ selectedPatient.age }} tuổi | {{ selectedPatient.gender }}</p>
                  <span>STT: {{ selectedPatient.queueNo }}</span>
                </div>
              </section>

              <section class="detail-grid">
                <div><p>Bác sĩ phụ trách</p><strong>{{ selectedPatient.doctor }}</strong></div>
                <div><p>Phòng khám</p><strong>{{ selectedPatient.room }} ({{ selectedPatient.specialty }})</strong></div>
                <div><p>Dịch vụ</p><strong>{{ selectedPatient.service }}</strong></div>
                <div><p>Loại lịch hẹn</p><strong>{{ selectedPatient.appointmentType }}</strong></div>
              </section>

              <section class="queue-timeline">
                <h4>Dòng thời gian hàng đợi</h4>
                <article class="done">
                  <span><mat-icon>check</mat-icon></span>
                  <div>
                    <strong>Check-in thành công</strong>
                    <p>{{ selectedPatient.checkInTime }} - Tiếp tân A</p>
                    <small>{{ selectedPatient.notes }}</small>
                  </div>
                </article>
                <article [class.active]="selectedPatient.status === 'IN_PROGRESS'" [class.done]="selectedPatient.status === 'COMPLETED'">
                  <span><mat-icon>medical_services</mat-icon></span>
                  <div>
                    <strong>{{ selectedPatient.status === 'WAITING' ? 'Chờ gọi vào khám' : 'Bắt đầu khám' }}</strong>
                    <p>{{ selectedPatient.doctor }}</p>
                    <small>{{ selectedPatient.status === 'WAITING' ? 'Bệnh nhân đang chờ đến lượt khám.' : 'Đang tiến hành khám lâm sàng và khai thác bệnh sử.' }}</small>
                  </div>
                </article>
                <article [class.done]="selectedPatient.status === 'COMPLETED'">
                  <span><mat-icon>flag</mat-icon></span>
                  <div>
                    <strong>Kết thúc dự kiến</strong>
                    <p>~ 09:15 AM</p>
                  </div>
                </article>
              </section>
            </div>

            <footer>
              <button type="button" (click)="cycleStatus()">Cập nhật trạng thái</button>
              <button type="button" aria-label="In phiếu"><mat-icon>print</mat-icon></button>
            </footer>
          </aside>
        </div>
      }
    </section>
  `,
  styles: []
})
export class QueueTrackingComponent {
  searchTerm = '';
  specialtyFilter = '';
  doctorFilter = '';
  selectedDate = '2026-07-24';
  page = 1;
  readonly pageSize = 4;
  selectedPatient: QueuePatient | null = null;

  readonly patients: QueuePatient[] = [
    { id: 1, queueNo: '#001', patientCode: 'BN-2026-8891', patientName: 'Lê Văn Tám', initials: 'LT', age: 45, gender: 'Nam', doctor: 'BS. Trần Mạnh Hùng', room: 'Phòng 204', specialty: 'Nội tổng quát', service: 'Khám nội khoa', appointmentType: 'Tái khám định kỳ', checkInTime: '08:15 AM', status: 'IN_PROGRESS', notes: 'Bệnh nhân đến đúng giờ, đã đo huyết áp 120/80.' },
    { id: 2, queueNo: '#002', patientCode: 'BN-2026-4412', patientName: 'Nguyễn Thu Hà', initials: 'NH', age: 31, gender: 'Nữ', doctor: 'BS. Phạm Minh Tuấn', room: 'Phòng 102', specialty: 'Nhi khoa', service: 'Khám nhi', appointmentType: 'Khám mới', checkInTime: '08:22 AM', status: 'WAITING', notes: 'Đã hoàn tất check-in và chờ gọi vào phòng khám.' },
    { id: 3, queueNo: '#003', patientCode: 'BN-2026-9003', patientName: 'Đỗ Văn Việt', initials: 'DV', age: 52, gender: 'Nam', doctor: 'BS. Trần Mạnh Hùng', room: 'Phòng 204', specialty: 'Nội tổng quát', service: 'Khám nội khoa', appointmentType: 'Khám mới', checkInTime: '08:30 AM', status: 'WAITING', notes: 'Bệnh nhân có kết quả xét nghiệm mang theo.' },
    { id: 4, queueNo: '#004', patientCode: 'BN-2026-1122', patientName: 'Trương Hoàng Yến', initials: 'TH', age: 28, gender: 'Nữ', doctor: 'BS. Lê Thu Thủy', room: 'Phòng 301', specialty: 'Sản phụ khoa', service: 'Khám sản', appointmentType: 'Tái khám', checkInTime: '08:05 AM', status: 'COMPLETED', notes: 'Đã hoàn tất khám và nhận chỉ định theo dõi.' },
    { id: 5, queueNo: '#005', patientCode: 'BN-2026-7104', patientName: 'Mai Quốc Bảo', initials: 'MB', age: 39, gender: 'Nam', doctor: 'BS. Nguyễn Minh Anh', room: 'Phòng 101', specialty: 'Nội tổng quát', service: 'Khám tổng quát', appointmentType: 'Khám mới', checkInTime: '08:42 AM', status: 'WAITING', notes: 'Đã cập nhật thông tin bảo hiểm.' }
  ];

  get specialties(): string[] {
    return Array.from(new Set(this.patients.map(patient => patient.specialty)));
  }

  get doctors(): string[] {
    return Array.from(new Set(this.patients.map(patient => patient.doctor)));
  }

  get filteredPatients(): QueuePatient[] {
    const term = this.searchTerm.trim().toLowerCase();
    return this.patients.filter(patient => {
      const matchesTerm = !term || `${patient.patientName} ${patient.patientCode} ${patient.doctor}`.toLowerCase().includes(term);
      const matchesSpecialty = !this.specialtyFilter || patient.specialty === this.specialtyFilter;
      const matchesDoctor = !this.doctorFilter || patient.doctor === this.doctorFilter;
      return matchesTerm && matchesSpecialty && matchesDoctor;
    });
  }

  get totalPages(): number {
    return Math.max(1, Math.ceil(this.filteredPatients.length / this.pageSize));
  }

  get pageNumbers(): number[] {
    return Array.from({ length: this.totalPages }, (_, index) => index + 1);
  }

  get pagedPatients(): QueuePatient[] {
    const safePage = Math.min(this.page, this.totalPages);
    const start = (safePage - 1) * this.pageSize;
    return this.filteredPatients.slice(start, start + this.pageSize);
  }

  applyFilters(): void {
    this.page = 1;
  }

  countStatus(status: QueueStatus): number {
    return this.filteredPatients.filter(patient => patient.status === status).length;
  }

  statusLabel(status: QueueStatus): string {
    if (status === 'WAITING') return 'Chờ khám';
    if (status === 'IN_PROGRESS') return 'Đang khám';
    return 'Đã hoàn thành';
  }

  openDetail(patient: QueuePatient): void {
    this.selectedPatient = patient;
  }

  closeDetail(): void {
    this.selectedPatient = null;
  }

  cycleStatus(): void {
    if (!this.selectedPatient) return;
    if (this.selectedPatient.status === 'WAITING') this.selectedPatient.status = 'IN_PROGRESS';
    else if (this.selectedPatient.status === 'IN_PROGRESS') this.selectedPatient.status = 'COMPLETED';
    else this.selectedPatient.status = 'WAITING';
  }
}

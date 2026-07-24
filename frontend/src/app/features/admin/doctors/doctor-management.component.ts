import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { NotificationService } from '../../../core/services/notification.service';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';
import { Doctor, DoctorService } from './doctor.service';

@Component({
  selector: 'app-doctor-management',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, MatDialogModule, MatIconModule],
  template: `
    <section class="doctor-page">
      <header class="page-header">
        <div>
          <span class="eyebrow">Quản trị phòng khám</span>
          <h1>Danh sách bác sĩ</h1>
          <p>Quản lý hồ sơ, chuyên khoa, phòng khám và trạng thái làm việc của bác sĩ.</p>
        </div>
        <div class="header-actions">
          <button class="ghost-button" type="button" (click)="exportCsv()" [disabled]="!doctors.length">
            <mat-icon>download</mat-icon>Xuất dữ liệu
          </button>
          <a class="primary-button" routerLink="/doctors/new">
            <mat-icon>add</mat-icon>Thêm bác sĩ
          </a>
        </div>
      </header>

      <div class="summary-grid">
        <article><span>Tổng bác sĩ</span><strong>{{ totalElements }}</strong><small>{{ activeDoctors }} đang hoạt động</small></article>
        <article><span>Chuyên khoa</span><strong>{{ specialties.length }}</strong><small>Lấy từ dữ liệu API hiện tại</small></article>
        <article><span>Trạng thái tải</span><strong>{{ loading ? '...' : doctors.length }}</strong><small>{{ loading ? 'Đang tải dữ liệu' : 'Bản ghi trong trang này' }}</small></article>
      </div>

      <section class="panel">
        <div class="filters">
          <label class="search-field">
            <mat-icon>search</mat-icon>
            <input [(ngModel)]="searchTerm" type="search" placeholder="Tìm theo tên, email, phòng" (keyup.enter)="loadDoctors(0)" />
          </label>
          <label>
            <span>Chuyên khoa</span>
            <input [(ngModel)]="selectedSpecialty" placeholder="Nhập chuyên khoa" (keyup.enter)="loadDoctors(0)" />
          </label>
          <label>
            <span>Trạng thái</span>
            <select [(ngModel)]="selectedStatus" (change)="loadDoctors(0)">
              <option value="ALL">Tất cả trạng thái</option>
              <option value="ACTIVE">Đang hoạt động</option>
              <option value="INACTIVE">Tạm khóa</option>
            </select>
          </label>
          <button class="filter-button" type="button" (click)="loadDoctors(0)">
            <mat-icon>filter_alt</mat-icon>Lọc
          </button>
        </div>

        @if (errorMessage) {
          <div class="alert">
            {{ errorMessage }}
          </div>
        }

        <div class="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Bác sĩ</th>
                <th>Chuyên khoa</th>
                <th>Phòng</th>
                <th>Tối đa/ngày</th>
                <th>Trạng thái</th>
                <th>Thao tác</th>
              </tr>
            </thead>
            <tbody>
              @if (loading) {
                <tr><td colspan="6" class="empty-state">Đang tải danh sách bác sĩ...</td></tr>
              } @else {
                @for (doctor of doctors; track doctor.id) {
                  <tr>
                    <td>
                      <div class="doctor-cell">
                        <span class="avatar">{{ initials(doctor.fullName) }}</span>
                        <div><strong>{{ doctor.fullName }}</strong><small>{{ doctor.email || ('User ID: ' + doctor.userId) }}</small></div>
                      </div>
                    </td>
                    <td>{{ doctor.specialty }}</td>
                    <td>{{ doctor.roomNumber }}</td>
                    <td>{{ doctor.maxPatientsPerDay }} bệnh nhân</td>
                    <td>
                      <span class="status" [class.inactive]="!doctor.active">
                        {{ doctor.active ? 'Đang hoạt động' : 'Tạm khóa' }}
                      </span>
                    </td>
                    <td>
                      <div class="row-actions">
                        <a [routerLink]="['/doctors', doctor.id]" title="Xem chi tiết"><mat-icon>visibility</mat-icon></a>
                        <a [routerLink]="['/doctors', doctor.id, 'edit']" title="Chỉnh sửa"><mat-icon>edit</mat-icon></a>
                        <button type="button" title="Quản lý lịch" disabled><mat-icon>event</mat-icon></button>
                        <button type="button" [title]="doctor.active ? 'Khóa bác sĩ' : 'Mở khóa bác sĩ'" (click)="toggleStatus(doctor)">
                          <mat-icon>{{ doctor.active ? 'lock' : 'lock_open' }}</mat-icon>
                        </button>
                      </div>
                    </td>
                  </tr>
                } @empty {
                  <tr><td colspan="6" class="empty-state">Không có dữ liệu bác sĩ từ API.</td></tr>
                }
              }
            </tbody>
          </table>
        </div>

        <footer class="pagination">
          <span>Hiển thị {{ rangeStart }}-{{ rangeEnd }} trong {{ totalElements }} bác sĩ</span>
          <div>
            <button type="button" [disabled]="pageIndex === 0 || loading" (click)="loadDoctors(pageIndex - 1)"><mat-icon>chevron_left</mat-icon></button>
            <strong>Trang {{ pageIndex + 1 }} / {{ totalPages }}</strong>
            <button type="button" [disabled]="pageIndex + 1 >= totalPages || loading" (click)="loadDoctors(pageIndex + 1)"><mat-icon>chevron_right</mat-icon></button>
          </div>
        </footer>
      </section>
    </section>
  `,
  styles: [`
    :host{display:block;color:#172033}.doctor-page{display:grid;gap:24px;max-width:1280px;margin:0 auto}.page-header{display:flex;justify-content:space-between;gap:24px;align-items:flex-start;padding:28px;border-radius:8px;background:linear-gradient(135deg,#0f766e,#1d4ed8);color:#fff;box-shadow:0 18px 44px rgba(15,23,42,.16)}.eyebrow{display:inline-flex;margin-bottom:10px;font-size:12px;font-weight:800;text-transform:uppercase;color:#c7f9ee}h1{margin:0;font-size:34px;line-height:1.15}p{max-width:680px;margin:10px 0 0;color:rgba(255,255,255,.82)}.header-actions,.row-actions,.pagination div{display:flex;align-items:center;gap:10px}.primary-button,.ghost-button,.filter-button{display:inline-flex;align-items:center;gap:8px;min-height:42px;padding:0 16px;border:0;border-radius:8px;font-weight:750;cursor:pointer;text-decoration:none}.primary-button{background:#22c55e;color:#052e16}.ghost-button{background:rgba(255,255,255,.14);color:#fff;border:1px solid rgba(255,255,255,.26)}.filter-button{align-self:end;background:#0f766e;color:#fff}.summary-grid{display:grid;grid-template-columns:repeat(3,1fr);gap:16px}.summary-grid article,.panel{border:1px solid #dbe3ef;border-radius:8px;background:#fff;box-shadow:0 10px 28px rgba(15,23,42,.08)}.summary-grid article{display:grid;gap:6px;padding:20px}.summary-grid span,.summary-grid small{color:#64748b;font-weight:650}.summary-grid strong{color:#0f172a;font-size:30px}.filters{display:grid;grid-template-columns:minmax(240px,1fr) 220px 180px auto;gap:14px;padding:18px;border-bottom:1px solid #e2e8f0;background:#f8fafc}label{display:grid;gap:7px;color:#475569;font-size:13px;font-weight:750}input,select{width:100%;min-height:44px;box-sizing:border-box;border:1px solid #cbd5e1;border-radius:8px;background:#fff;color:#0f172a;padding:0 12px;font:inherit}.search-field{position:relative;align-self:end}.search-field mat-icon{position:absolute;left:12px;bottom:10px;color:#94a3b8}.search-field input{padding-left:44px}.alert{margin:16px 18px 0;padding:12px 14px;border:1px solid #fecaca;border-radius:8px;background:#fff1f2;color:#991b1b;font-weight:700}.table-wrap{overflow-x:auto}table{width:100%;border-collapse:collapse;min-width:900px}th,td{padding:16px 18px;text-align:left;border-bottom:1px solid #edf2f7}th{color:#475569;font-size:12px;text-transform:uppercase}td{color:#263244;font-weight:600}.doctor-cell{display:flex;align-items:center;gap:12px;min-width:250px}.avatar{display:inline-grid;place-items:center;width:44px;height:44px;border-radius:50%;background:#0f766e;color:#fff;font-weight:850}.doctor-cell strong,.doctor-cell small{display:block}.doctor-cell small{margin-top:3px;color:#64748b;font-weight:500}.status{display:inline-flex;align-items:center;min-height:28px;padding:0 10px;border-radius:999px;background:#dcfce7;color:#166534;font-size:12px;font-weight:800}.status.inactive{background:#fee2e2;color:#991b1b}.row-actions a,.row-actions button,.pagination button{display:inline-grid;place-items:center;width:36px;height:36px;border:1px solid #dbe3ef;border-radius:8px;background:#fff;color:#2563eb;cursor:pointer;text-decoration:none}.row-actions button:disabled{color:#94a3b8;cursor:not-allowed}.empty-state{padding:42px 18px;text-align:center;color:#64748b}.pagination{display:flex;justify-content:space-between;align-items:center;gap:16px;padding:16px 18px;color:#64748b;font-weight:650;background:#fff}.pagination strong{color:#1e293b}@media(max-width:900px){.page-header,.pagination{flex-direction:column;align-items:stretch}.header-actions{justify-content:flex-start;flex-wrap:wrap}.summary-grid,.filters{grid-template-columns:1fr}}
  `]
})
export class DoctorManagementComponent implements OnInit {
  doctors: Doctor[] = [];
  totalElements = 0;
  totalPages = 1;
  pageIndex = 0;
  readonly pageSize = 10;
  searchTerm = '';
  selectedSpecialty = '';
  selectedStatus: 'ALL' | 'ACTIVE' | 'INACTIVE' = 'ALL';
  loading = false;
  errorMessage = '';

  constructor(
    private doctorService: DoctorService,
    private notification: NotificationService,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    this.loadDoctors(0);
  }

  get specialties(): string[] {
    return Array.from(new Set(this.doctors.map(doctor => doctor.specialty).filter(Boolean)));
  }

  get activeDoctors(): number {
    return this.doctors.filter(doctor => doctor.active).length;
  }

  get rangeStart(): number {
    return this.totalElements === 0 ? 0 : this.pageIndex * this.pageSize + 1;
  }

  get rangeEnd(): number {
    return Math.min((this.pageIndex + 1) * this.pageSize, this.totalElements);
  }

  loadDoctors(page: number): void {
    this.loading = true;
    this.errorMessage = '';
    this.pageIndex = Math.max(0, page);
    this.doctorService.getAll({
      page: this.pageIndex,
      size: this.pageSize,
      search: this.searchTerm,
      specialty: this.selectedSpecialty,
      active: this.selectedStatus === 'ALL' ? undefined : this.selectedStatus === 'ACTIVE'
    }).subscribe({
      next: (res) => {
        const data = res.data;
        this.doctors = data?.content || [];
        this.totalElements = data?.totalElements || 0;
        this.totalPages = Math.max(1, data?.totalPages || 1);
        this.loading = false;
      },
      error: () => {
        this.doctors = [];
        this.totalElements = 0;
        this.totalPages = 1;
        this.loading = false;
        this.errorMessage = 'Chưa tải được dữ liệu bác sĩ. Kiểm tra backend Doctor API đã sẵn sàng chưa.';
      }
    });
  }

  toggleStatus(doctor: Doctor): void {
    const action = doctor.active ? 'khóa' : 'mở khóa';
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: doctor.active ? 'Khóa bác sĩ' : 'Mở khóa bác sĩ',
        message: `Bạn có chắc muốn ${action} "${doctor.fullName}"?`
      }
    });

    dialogRef.afterClosed().subscribe(confirmed => {
      if (!confirmed) return;
      const request = doctor.active ? this.doctorService.deactivate(doctor.id) : this.doctorService.activate(doctor.id);
      request.subscribe({
        next: () => {
          this.notification.success(`Đã ${action} bác sĩ`);
          this.loadDoctors(this.pageIndex);
        },
        error: (err) => this.notification.error(err.error?.message || `Không thể ${action} bác sĩ`)
      });
    });
  }

  initials(name: string): string {
    return (name || '?').split(' ').slice(-2).map(part => part[0]).join('').toUpperCase();
  }

  exportCsv(): void {
    const header = ['Full name', 'Email', 'Specialty', 'Room', 'Max patients/day', 'Active'];
    const rows = this.doctors.map(doctor => [
      doctor.fullName,
      doctor.email || doctor.userId || '',
      doctor.specialty,
      doctor.roomNumber,
      doctor.maxPatientsPerDay,
      doctor.active ? 'ACTIVE' : 'INACTIVE'
    ]);
    const csv = [header, ...rows].map(row => row.map(value => `"${String(value).replace(/"/g, '""')}"`).join(',')).join('\n');
    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.download = 'doctors.csv';
    link.click();
    URL.revokeObjectURL(link.href);
  }
}

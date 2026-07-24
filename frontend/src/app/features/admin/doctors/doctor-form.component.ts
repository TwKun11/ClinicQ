import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { NotificationService } from '../../../core/services/notification.service';
import { User, UserService } from '../../users/user.service';
import { DoctorService } from './doctor.service';

@Component({
  selector: 'app-doctor-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, MatIconModule, MatSlideToggleModule],
  template: `
    <section class="doctor-form-page">
      <header class="page-title">
        <a routerLink="/doctors"><mat-icon>arrow_back</mat-icon>Quay lại</a>
        <div>
          <span>Quản lý bác sĩ</span>
          <h1>{{ isEdit ? 'Chỉnh sửa bác sĩ' : 'Thêm bác sĩ' }}</h1>
          <p>{{ isEdit ? 'Cập nhật hồ sơ chuyên môn của bác sĩ.' : 'Chọn tài khoản người dùng có sẵn rồi khai báo thông tin chuyên môn.' }}</p>
        </div>
      </header>

      <form [formGroup]="form" (ngSubmit)="save()">
        <div class="form-grid">
          <section class="panel account-panel">
            <div class="section-heading">
              <mat-icon>account_circle</mat-icon>
              <h2>Tài khoản bác sĩ</h2>
            </div>
            <p>Chọn một tài khoản người dùng đã tồn tại trong hệ thống để chỉ định vai trò bác sĩ.</p>

            <label>
              <span>Tìm kiếm người dùng</span>
              <div class="search-box">
                <mat-icon>person_search</mat-icon>
                <input
                  type="search"
                  [value]="userSearch"
                  (input)="onUserSearch($event)"
                  placeholder="Nhập tên hoặc email..."
                  [disabled]="isEdit"
                />
              </div>
            </label>

            @if (userLoadError) {
              <div class="alert">{{ userLoadError }}</div>
            }

            <div class="user-list">
              @for (user of filteredUsers; track user.id) {
                <button
                  type="button"
                  class="user-option"
                  [class.selected]="selectedUser?.id === user.id"
                  [disabled]="isEdit"
                  (click)="selectUser(user)"
                >
                  <span class="avatar">{{ initials(user.fullName || user.email) }}</span>
                  <span>
                    <strong>{{ user.fullName || user.username }}</strong>
                    <small>{{ user.email }}</small>
                  </span>
                  @if (selectedUser?.id === user.id) {
                    <mat-icon>check_circle</mat-icon>
                  }
                </button>
              } @empty {
                <div class="empty-user">Không tìm thấy người dùng phù hợp.</div>
              }
            </div>
          </section>

          <section class="panel professional-panel">
            <div class="section-heading wide">
              <mat-icon>clinical_notes</mat-icon>
              <h2>Thông tin chuyên môn</h2>
            </div>

            <div class="field wide" [class.invalid]="form.controls.specialty.invalid && form.controls.specialty.touched">
              <label>Chuyên khoa <b>*</b></label>
              <div class="select-wrap">
                <select formControlName="specialty">
                  <option value="" disabled>Chọn chuyên khoa...</option>
                  @for (specialty of specialties; track specialty) {
                    <option [value]="specialty">{{ specialty }}</option>
                  }
                </select>
                <mat-icon>expand_more</mat-icon>
              </div>
              @if (form.controls.specialty.invalid && form.controls.specialty.touched) {
                <small><mat-icon>error</mat-icon>Specialty is required</small>
              }
            </div>

            <label class="field">
              <span>Số phòng</span>
              <input formControlName="roomNumber" placeholder="Ví dụ: P.102" />
            </label>

            <label class="field">
              <span>Số bệnh nhân tối đa/ngày</span>
              <input type="number" formControlName="maxPatientsPerDay" min="1" max="200" />
            </label>

            <div class="status-row wide">
              <div>
                <strong>Trạng thái hoạt động</strong>
                <span>Bác sĩ sẽ xuất hiện trong danh sách đặt lịch</span>
              </div>
              <mat-slide-toggle formControlName="active"></mat-slide-toggle>
            </div>
          </section>
        </div>

        <footer class="sticky-actions">
          <button type="button" class="cancel-button" (click)="router.navigate(['/doctors'])">Hủy</button>
          <button type="submit" class="save-button" [disabled]="form.invalid || loading || !selectedUser">
            <mat-icon>save</mat-icon>
            {{ loading ? 'Đang lưu...' : 'Lưu bác sĩ' }}
          </button>
        </footer>
      </form>
    </section>
  `,
  styles: [`
    :host{display:block;color:#191c1d}.doctor-form-page{max-width:1120px;margin:0 auto;display:grid;gap:24px}.page-title{display:flex;gap:18px;align-items:flex-start}.page-title a{display:inline-flex;align-items:center;gap:6px;color:#004e9f;text-decoration:none;font-weight:800}.page-title span{color:#727784;font-size:12px;font-weight:800}.page-title h1{margin:4px 0 6px;font-size:28px}.page-title p{margin:0;color:#414753}.form-grid{display:grid;grid-template-columns:360px minmax(0,1fr);gap:24px}.panel{border:1px solid #c1c6d5;border-radius:12px;background:#fff;padding:24px;box-shadow:0 1px 3px rgba(0,0,0,.05)}.section-heading{display:flex;align-items:center;gap:8px;margin-bottom:12px;color:#004e9f}.section-heading h2{margin:0;font-size:18px}.account-panel>p{margin:0 0 24px;color:#414753;line-height:1.5}label,.field{display:grid;gap:7px;color:#727784;font-size:12px;font-weight:800}.search-box{position:relative}.search-box mat-icon{position:absolute;left:12px;top:11px;color:#727784}.search-box input{padding-left:44px}.user-list{display:grid;gap:10px;margin-top:16px}.user-option{display:grid;grid-template-columns:48px 1fr 24px;gap:12px;align-items:center;width:100%;padding:12px;border:1px solid #c1c6d5;border-radius:8px;background:#fff;text-align:left;cursor:pointer}.user-option.selected{border-color:#004e9f;background:#eff6ff}.user-option:disabled{cursor:default}.avatar{display:grid;place-items:center;width:48px;height:48px;border-radius:50%;background:#004e9f;color:#fff;font-weight:850}.user-option strong,.user-option small{display:block}.user-option small{margin-top:3px;color:#727784}.user-option mat-icon{color:#004e9f}.professional-panel{display:grid;grid-template-columns:1fr 1fr;gap:24px;align-content:start}.wide{grid-column:1/-1}input,select{width:100%;min-height:44px;box-sizing:border-box;border:1px solid #c1c6d5;border-radius:8px;background:#fff;color:#191c1d;padding:0 12px;font:inherit;outline:none}input:focus,select:focus{border-color:#004e9f;box-shadow:0 0 0 3px rgba(0,78,159,.14)}.select-wrap{position:relative}.select-wrap select{appearance:none}.select-wrap mat-icon{position:absolute;right:12px;top:10px;color:#727784;pointer-events:none}.invalid select{border-color:#ba1a1a}.field b{color:#ba1a1a}.field small{display:flex;align-items:center;gap:4px;color:#ba1a1a}.field small mat-icon{font-size:15px;width:15px;height:15px}.status-row{display:flex;justify-content:space-between;align-items:center;gap:16px;padding:16px;border:1px solid #c1c6d5;border-radius:8px;background:#f3f4f5}.status-row strong,.status-row span{display:block}.status-row span{margin-top:4px;color:#727784;font-size:12px}.sticky-actions{display:flex;justify-content:flex-end;gap:12px;padding-top:24px;border-top:1px solid #c1c6d5}.cancel-button,.save-button{display:inline-flex;align-items:center;gap:8px;min-height:42px;padding:0 24px;border:0;border-radius:999px;font-weight:800;cursor:pointer}.cancel-button{background:transparent;color:#414753}.save-button{background:#004e9f;color:#fff}.save-button:disabled{background:#c1c6d5;cursor:not-allowed}.alert,.empty-user{padding:12px;border-radius:8px;font-weight:700}.alert{margin-top:12px;background:#fff1f2;color:#991b1b}.empty-user{background:#f3f4f5;color:#727784;text-align:center}@media(max-width:960px){.form-grid,.professional-panel{grid-template-columns:1fr}.page-title{flex-direction:column}.sticky-actions{justify-content:stretch}.cancel-button,.save-button{justify-content:center;flex:1}}
  `]
})
export class DoctorFormComponent implements OnInit {
  isEdit = false;
  loading = false;
  doctorId: number | null = null;
  users: User[] = [];
  selectedUser: User | null = null;
  userSearch = '';
  userLoadError = '';

  readonly specialties = ['Nội tổng quát', 'Nhi khoa', 'Sản phụ khoa', 'Tai Mũi Họng', 'Tim mạch', 'Da liễu'];

  form = this.fb.group({
    userId: [null as number | null, Validators.required],
    specialty: ['', Validators.required],
    roomNumber: ['', Validators.required],
    maxPatientsPerDay: [30, [Validators.required, Validators.min(1), Validators.max(200)]],
    active: [true]
  });

  constructor(
    private fb: FormBuilder,
    private doctorService: DoctorService,
    private userService: UserService,
    private route: ActivatedRoute,
    public router: Router,
    private notification: NotificationService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    this.isEdit = !!id;
    this.doctorId = id ? Number(id) : null;
    this.loadUsers();
    if (this.doctorId) {
      this.loadDoctor(this.doctorId);
    }
  }

  get filteredUsers(): User[] {
    const term = this.userSearch.trim().toLowerCase();
    return this.users
      .filter(user => !term || `${user.fullName} ${user.email} ${user.username}`.toLowerCase().includes(term))
      .slice(0, 6);
  }

  loadUsers(): void {
    this.userService.getAll(0, 100).subscribe({
      next: (res) => this.users = res.data?.content || [],
      error: () => this.userLoadError = 'Không tải được danh sách người dùng.'
    });
  }

  loadDoctor(id: number): void {
    this.loading = true;
    this.doctorService.getById(id).subscribe({
      next: (res) => {
        const doctor = res.data;
        if (doctor) {
          this.form.patchValue({
            userId: doctor.userId || null,
            specialty: doctor.specialty,
            roomNumber: doctor.roomNumber,
            maxPatientsPerDay: doctor.maxPatientsPerDay,
            active: doctor.active
          });
          this.selectedUser = {
            id: doctor.userId || 0,
            username: doctor.email,
            email: doctor.email,
            fullName: doctor.fullName,
            role: 'STAFF',
            active: doctor.active,
            createdAt: doctor.createdAt || ''
          };
          this.userSearch = doctor.fullName;
        }
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.notification.error('Không tải được thông tin bác sĩ');
      }
    });
  }

  onUserSearch(event: Event): void {
    this.userSearch = (event.target as HTMLInputElement).value;
  }

  selectUser(user: User): void {
    this.selectedUser = user;
    this.userSearch = user.fullName || user.email;
    this.form.patchValue({ userId: user.id });
  }

  save(): void {
    if (this.form.invalid || !this.selectedUser) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading = true;
    const value = this.form.getRawValue();
    const payload = {
      userId: value.userId!,
      specialty: value.specialty || '',
      roomNumber: value.roomNumber || '',
      maxPatientsPerDay: value.maxPatientsPerDay || 1,
      active: value.active ?? true
    };
    const request = this.isEdit && this.doctorId
      ? this.doctorService.update(this.doctorId, payload)
      : this.doctorService.create(payload);
    request.subscribe({
      next: () => {
        this.notification.success(this.isEdit ? 'Đã cập nhật bác sĩ' : 'Đã thêm bác sĩ');
        this.router.navigate(['/doctors']);
      },
      error: (err) => {
        this.loading = false;
        this.notification.error(err.error?.message || 'Không lưu được bác sĩ');
      }
    });
  }

  initials(name: string): string {
    return (name || '?').split(' ').slice(-2).map(part => part[0]).join('').toUpperCase();
  }
}

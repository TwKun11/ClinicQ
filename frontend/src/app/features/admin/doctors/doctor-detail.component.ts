import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { Doctor, DoctorService } from './doctor.service';

@Component({
  selector: 'app-doctor-detail',
  standalone: true,
  imports: [RouterLink, MatIconModule],
  template: `
    <section class="detail-page">
      <a class="back-link" routerLink="/doctors"><mat-icon>arrow_back</mat-icon>Quay lại</a>

      @if (loading) {
        <p>Đang tải thông tin bác sĩ...</p>
      } @else if (doctor) {
        <header>
          <span class="avatar">{{ initials(doctor.fullName) }}</span>
          <div>
            <h1>{{ doctor.fullName }}</h1>
            <p>{{ doctor.email }}</p>
          </div>
          <a class="edit-button" [routerLink]="['/doctors', doctor.id, 'edit']"><mat-icon>edit</mat-icon>Chỉnh sửa</a>
        </header>

        <dl>
          <div><dt>Chuyên khoa</dt><dd>{{ doctor.specialty }}</dd></div>
          <div><dt>Phòng khám</dt><dd>{{ doctor.roomNumber }}</dd></div>
          <div><dt>Tối đa/ngày</dt><dd>{{ doctor.maxPatientsPerDay }} bệnh nhân</dd></div>
          <div><dt>Trạng thái</dt><dd>{{ doctor.active ? 'Đang hoạt động' : 'Tạm khóa' }}</dd></div>
        </dl>
      } @else {
        <p>Không tìm thấy thông tin bác sĩ từ API.</p>
      }
    </section>
  `,
  styles: [`
    .detail-page{max-width:860px;margin:0 auto;padding:28px;border:1px solid #dbe3ef;border-radius:8px;background:#fff;box-shadow:0 10px 28px rgba(15,23,42,.08)}.back-link,.edit-button{display:inline-flex;align-items:center;gap:6px;color:#0f766e;text-decoration:none;font-weight:800}header{display:flex;align-items:center;gap:16px;margin:20px 0 24px}.avatar{display:grid;place-items:center;width:64px;height:64px;border-radius:50%;background:#0f766e;color:#fff;font-size:22px;font-weight:850}h1,p{margin:0}p{color:#64748b}.edit-button{margin-left:auto;border:1px solid #dbe3ef;border-radius:8px;padding:10px 12px}dl{display:grid;grid-template-columns:repeat(2,1fr);gap:14px}dl div{padding:16px;border:1px solid #e2e8f0;border-radius:8px}dt{color:#64748b;font-weight:750}dd{margin:6px 0 0;color:#0f172a;font-size:18px;font-weight:800}@media(max-width:720px){header{align-items:flex-start;flex-direction:column}.edit-button{margin-left:0}dl{grid-template-columns:1fr}}
  `]
})
export class DoctorDetailComponent implements OnInit {
  doctor: Doctor | null = null;
  loading = true;

  constructor(private route: ActivatedRoute, private doctorService: DoctorService) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.doctorService.getById(id).subscribe({
      next: (res) => {
        this.doctor = res.data || null;
        this.loading = false;
      },
      error: () => {
        this.doctor = null;
        this.loading = false;
      }
    });
  }

  initials(name: string): string {
    return (name || '?').split(' ').slice(-2).map(part => part[0]).join('').toUpperCase();
  }
}

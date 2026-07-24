import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

type QueueStatus = 'WAITING' | 'IN_PROGRESS';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatIconModule],
  template: `
    <section class="dashboard-page">
      <header class="hero">
        <div>
          <span>Admin / Tổng quan</span>
          <h1>Chào buổi sáng, Admin</h1>
          <p>Đây là những gì đang diễn ra tại phòng khám hôm nay.</p>
        </div>
        <div class="actions">
          <b><mat-icon>calendar_today</mat-icon>Hôm nay, 24 Tháng 5 2024</b>
          <button mat-flat-button type="button"><mat-icon>refresh</mat-icon>Làm mới dữ liệu</button>
        </div>
      </header>

      <div class="kpis">
        <article><mat-icon>calendar_month</mat-icon><small>+12%</small><p>Lịch hẹn hôm nay</p><strong>125</strong></article>
        <article><mat-icon>hourglass_empty</mat-icon><small class="down">-5%</small><p>Bệnh nhân đang chờ</p><strong>12</strong></article>
        <article><mat-icon>check_circle</mat-icon><small>+8%</small><p>Ca khám đã hoàn thành</p><strong>45</strong></article>
      </div>

      <div class="main-grid">
        <section class="panel queue">
          <div class="panel-title"><h2>Hàng đợi hiện tại</h2><button type="button">Xem tất cả</button></div>
          <div class="table-wrap">
            <table>
              <thead><tr><th>STT</th><th>Bệnh nhân</th><th>Bác sĩ</th><th>Phòng</th><th>Thời gian chờ</th><th>Trạng thái</th></tr></thead>
              <tbody>
                @for (row of queueRows; track row.index) {
                  <tr>
                    <td>{{ row.index }}</td>
                    <td><strong>{{ row.patientName }}</strong></td>
                    <td>{{ row.doctorName }}</td>
                    <td>{{ row.room }}</td>
                    <td>{{ row.waitingTime }}</td>
                    <td><span class="badge" [class.progress]="row.status === 'IN_PROGRESS'"><i></i>{{ row.status }}</span></td>
                  </tr>
                }
              </tbody>
            </table>
          </div>
        </section>

        <aside>
          <section class="panel chart">
            <h2>Trạng thái lịch hẹn</h2>
            <div class="donut">
              <svg viewBox="0 0 36 36">
                <circle class="bg" cx="18" cy="18" r="15.915"></circle>
                <circle class="booked" cx="18" cy="18" r="15.915"></circle>
                <circle class="examining" cx="18" cy="18" r="15.915"></circle>
                <circle class="done" cx="18" cy="18" r="15.915"></circle>
                <circle class="canceled" cx="18" cy="18" r="15.915"></circle>
              </svg>
              <div><strong>125</strong><span>Tổng</span></div>
            </div>
            <div class="legend">
              <span><i></i>Đã đặt: 40%</span><span><i></i>Khám: 25%</span>
              <span><i></i>Xong: 20%</span><span><i></i>Hủy: 15%</span>
            </div>
          </section>

          <section class="panel activity">
            <h2>Hoạt động gần đây</h2>
            @for (item of activities; track item.message) {
              <article [class]="item.tone"><i></i><p>{{ item.message }}</p><span>{{ item.time }}</span></article>
            }
            <button type="button">Xem tất cả nhật ký</button>
          </section>
        </aside>
      </div>

      <footer><span>© 2024 ClinicQ Health Management System.</span><span><i></i>Hệ thống đang hoạt động ổn định</span></footer>
    </section>
  `,
  styles: [`
    :host{display:block;color:#191c1d}.dashboard-page{max-width:1440px;margin:0 auto;display:grid;gap:24px}
    .hero,.actions,.panel-title,footer,footer span{display:flex;align-items:center}.hero{justify-content:space-between;gap:24px}
    .hero span{color:#005cba;font-size:12px;font-weight:800}.hero h1{margin:8px 0 6px;font-size:32px;line-height:1.2}.hero p{margin:0;color:#414753}
    .actions{gap:12px;flex-wrap:wrap;justify-content:flex-end}.actions b{display:flex;gap:8px;align-items:center;min-height:40px;padding:0 14px;border:1px solid #c1c6d5;border-radius:8px;background:#fff;color:#414753}.actions button{background:#004e9f;color:#fff;border-radius:8px;font-weight:800}
    .kpis{display:grid;grid-template-columns:repeat(3,1fr);gap:24px}.kpis article,.panel{border:1px solid #c1c6d5;border-radius:8px;background:#fff;box-shadow:0 10px 24px rgba(25,28,29,.06)}
    .kpis article{position:relative;padding:24px}.kpis mat-icon{display:grid;place-items:center;width:48px;height:48px;border-radius:8px;background:#dfe8ff;color:#004e9f;font-size:28px}.kpis small{position:absolute;right:24px;top:24px;color:#006a61;font-weight:850}.kpis .down{color:#ba1a1a}.kpis p{margin:18px 0 6px;color:#414753;font-weight:700}.kpis strong{font-size:36px}
    .main-grid{display:grid;grid-template-columns:minmax(0,2fr) minmax(320px,1fr);gap:24px}aside{display:grid;gap:24px;align-content:start}.panel{overflow:hidden}.panel-title{justify-content:space-between;padding:18px 24px;border-bottom:1px solid #c1c6d5}h2{margin:0;font-size:18px}.panel-title button,.activity button{border:0;background:transparent;color:#004e9f;font-weight:800;cursor:pointer}
    .table-wrap{overflow-x:auto}table{width:100%;min-width:760px;border-collapse:collapse}th,td{padding:15px 24px;text-align:left;border-bottom:1px solid #e1e3e4}th{background:#f3f4f5;color:#414753;font-size:12px;text-transform:uppercase}td{color:#414753;font-weight:600}td strong{color:#191c1d}
    .badge{display:inline-flex;align-items:center;gap:7px;min-height:30px;padding:0 12px;border-radius:999px;background:#dfe8ff;color:#004e9f;font-size:12px;font-weight:850}.badge.progress{background:#d8fbf6;color:#006a61}
    .chart,.activity{padding:24px}.donut{position:relative;width:220px;height:220px;margin:18px auto}.donut svg{width:100%;height:100%;transform:rotate(-90deg)}circle{fill:transparent;stroke-width:3}.bg{stroke:#e1e3e4}.booked{stroke:#004e9f;stroke-dasharray:40 100}.examining{stroke:#006a61;stroke-dasharray:25 100;stroke-dashoffset:-40}.done{stroke:#3e32d5;stroke-dasharray:20 100;stroke-dashoffset:-65}.canceled{stroke:#ba1a1a;stroke-dasharray:15 100;stroke-dashoffset:-85}.donut div{position:absolute;inset:0;display:grid;place-content:center;text-align:center}.donut strong{font-size:22px}.donut span{color:#414753;font-size:11px;font-weight:850;text-transform:uppercase}
    .legend{display:grid;grid-template-columns:1fr 1fr;gap:10px;color:#414753;font-size:12px;font-weight:700}.legend span{display:flex;align-items:center;gap:8px}.legend span:nth-child(2) i,.badge.progress i,.activity .secondary i,footer i{background:#006a61}.legend span:nth-child(3) i{background:#3e32d5}.legend span:nth-child(4) i,.activity .danger i{background:#ba1a1a}
    i{display:inline-block;width:8px;height:8px;border-radius:50%;background:#004e9f;flex:0 0 auto}.activity article{display:grid;grid-template-columns:10px 1fr;gap:5px 12px;align-items:start;margin-top:18px}.activity article span{grid-column:2;color:#727784;font-size:12px;font-weight:700}.activity p{margin:0;color:#191c1d;line-height:1.45}.activity .neutral i{background:#727784}.activity button{width:100%;min-height:40px;margin-top:22px;border:1px solid #c1c6d5;border-radius:8px;background:#fff;color:#414753}
    footer{justify-content:space-between;gap:16px;padding-top:24px;border-top:1px solid #c1c6d5;color:#414753;font-size:12px;font-weight:700}footer span{gap:8px}
    @media(max-width:1100px){.main-grid{grid-template-columns:1fr}}@media(max-width:860px){.hero,footer{align-items:stretch;flex-direction:column}.actions{justify-content:flex-start}.kpis{grid-template-columns:1fr}}
  `]
})
export class DashboardComponent {
  readonly queueRows: Array<{ index: string; patientName: string; doctorName: string; room: string; waitingTime: string; status: QueueStatus }> = [
    { index: '#01', patientName: 'Nguyễn Văn An', doctorName: 'BS. Trần Hùng', room: 'Phòng 204', waitingTime: '15 phút', status: 'IN_PROGRESS' },
    { index: '#02', patientName: 'Lê Thị Mai', doctorName: 'BS. Phạm Thu', room: 'Phòng 102', waitingTime: '25 phút', status: 'WAITING' },
    { index: '#03', patientName: 'Hoàng Văn Thái', doctorName: 'BS. Trần Hùng', room: 'Phòng 204', waitingTime: '32 phút', status: 'WAITING' },
    { index: '#04', patientName: 'Đỗ Kim Liên', doctorName: 'BS. Nguyễn Nam', room: 'Phòng 301', waitingTime: '40 phút', status: 'WAITING' }
  ];

  readonly activities = [
    { tone: 'primary', message: 'Admin đã tạo bác sĩ mới Trần Quốc Toản', time: '2 phút trước' },
    { tone: 'secondary', message: 'Lịch hẹn B023 đã check-in thành công', time: '15 phút trước' },
    { tone: 'neutral', message: 'Hệ thống đã tự động sao lưu dữ liệu', time: '1 giờ trước' },
    { tone: 'danger', message: 'Cảnh báo: Phòng khám 302 mất kết nối', time: '3 giờ trước' }
  ];
}

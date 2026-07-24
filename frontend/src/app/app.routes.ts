import { Routes } from '@angular/router';
import { adminGuard, authGuard, userStaffGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./layout/main-layout/main-layout.component').then(m => m.MainLayoutComponent),
    canActivate: [authGuard],
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      {
        path: 'home',
        loadComponent: () => import('./features/home/home.component').then(m => m.HomeComponent)
      },
      {
        path: 'dashboard',
        canActivate: [adminGuard],
        loadComponent: () => import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent)
      },
      {
        path: 'users',
        canActivate: [adminGuard],
        loadComponent: () => import('./features/users/user-list/user-list.component').then(m => m.UserListComponent)
      },
      {
        path: 'users/new',
        canActivate: [adminGuard],
        loadComponent: () => import('./features/users/user-form/user-form.component').then(m => m.UserFormComponent)
      },
      {
        path: 'users/:id/edit',
        canActivate: [adminGuard],
        loadComponent: () => import('./features/users/user-form/user-form.component').then(m => m.UserFormComponent)
      },
      {
        path: 'doctors',
        canActivate: [adminGuard],
        loadComponent: () => import('./features/admin/doctors/doctor-management.component').then(m => m.DoctorManagementComponent)
      },
      {
        path: 'doctors/new',
        canActivate: [adminGuard],
        loadComponent: () => import('./features/admin/doctors/doctor-form.component').then(m => m.DoctorFormComponent)
      },
      {
        path: 'doctors/:id',
        canActivate: [adminGuard],
        loadComponent: () => import('./features/admin/doctors/doctor-detail.component').then(m => m.DoctorDetailComponent)
      },
      {
        path: 'doctors/:id/edit',
        canActivate: [adminGuard],
        loadComponent: () => import('./features/admin/doctors/doctor-form.component').then(m => m.DoctorFormComponent)
      },
      {
        path: 'schedules',
        canActivate: [adminGuard],
        loadComponent: () => import('./features/admin/schedules/schedule-management.component').then(m => m.ScheduleManagementComponent)
      },
      {
        path: 'queues',
        canActivate: [adminGuard],
        loadComponent: () => import('./features/admin/queues/queue-tracking.component').then(m => m.QueueTrackingComponent)
      },
      {
        path: 'change-password',
        canActivate: [userStaffGuard],
        loadComponent: () => import('./features/auth/change-password/change-password.component').then(m => m.ChangePasswordComponent)
      }
    ]
  },
  {
    path: '',
    loadComponent: () => import('./layout/auth-layout/auth-layout.component').then(m => m.AuthLayoutComponent),
    children: [
      {
        path: 'login',
        loadComponent: () => import('./features/auth/login/login.component').then(m => m.LoginComponent)
      },
      {
        path: 'register',
        loadComponent: () => import('./features/auth/register/register.component').then(m => m.RegisterComponent)
      },
      {
        path: 'forgot-password',
        loadComponent: () => import('./features/auth/forgot-password/forgot-password.component').then(m => m.ForgotPasswordComponent)
      },
      {
        path: 'reset-password',
        loadComponent: () => import('./features/auth/reset-password/reset-password.component').then(m => m.ResetPasswordComponent)
      }
    ]
  },
  { path: '**', redirectTo: '' }
];

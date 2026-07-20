import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';
import { ShellComponent } from './layout/shell/shell.component';
import { LoginComponent } from './features/login/login.component';
import { DashboardComponent } from './features/dashboard/dashboard.component';
import { ReconciliationComponent } from './features/reconciliation/reconciliation.component';
import { VarianceDetailComponent } from './features/variance-detail/variance-detail.component';
import { TransactionsComponent } from './features/transactions/transactions.component';
import { AnomaliesComponent } from './features/anomalies/anomalies.component';
import { TerminalHealthComponent } from './features/terminal-health/terminal-health.component';
import { DepotAssignmentsComponent } from './features/depot-assignments/depot-assignments.component';
import { ReportsComponent } from './features/reports/reports.component';
import { AuditLogsComponent } from './features/audit-logs/audit-logs.component';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  {
    path: '',
    component: ShellComponent,
    canActivate: [authGuard],
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
      { path: 'dashboard', component: DashboardComponent },
      {
        path: 'reconciliation',
        component: ReconciliationComponent,
        canActivate: [roleGuard('ADMIN', 'FINANCE_USER', 'AUDITOR')],
      },
      {
        path: 'variances/:id',
        component: VarianceDetailComponent,
        canActivate: [roleGuard('ADMIN', 'FINANCE_USER', 'AUDITOR')],
      },
      { path: 'transactions', component: TransactionsComponent },
      { path: 'anomalies', component: AnomaliesComponent },
      { path: 'terminal-health', component: TerminalHealthComponent },
      {
        path: 'depot-assignments',
        component: DepotAssignmentsComponent,
        canActivate: [roleGuard('ADMIN', 'FINANCE_USER', 'OPERATIONS_USER')],
      },
      {
        path: 'reports',
        component: ReportsComponent,
        canActivate: [roleGuard('ADMIN', 'FINANCE_USER', 'AUDITOR')],
      },
      {
        path: 'audit-logs',
        component: AuditLogsComponent,
        canActivate: [roleGuard('ADMIN', 'AUDITOR')],
      },
    ],
  },
  { path: '**', redirectTo: 'dashboard' },
];

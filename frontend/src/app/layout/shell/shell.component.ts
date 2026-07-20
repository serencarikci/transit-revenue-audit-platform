import { Component, computed, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatListModule } from '@angular/material/list';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { AuthService } from '../../core/auth/auth.service';

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    MatToolbarModule,
    MatSidenavModule,
    MatListModule,
    MatButtonModule,
    MatIconModule,
  ],
  templateUrl: './shell.component.html',
  styleUrl: './shell.component.scss',
})
export class ShellComponent {
  private readonly auth = inject(AuthService);
  readonly username = computed(() => this.auth.username());
  readonly role = computed(() => this.auth.role());

  readonly links = [
    { path: '/dashboard', label: 'Dashboard', icon: 'dashboard' },
    { path: '/reconciliation', label: 'Reconciliation', icon: 'account_balance' },
    { path: '/transactions', label: 'Transactions', icon: 'search' },
    { path: '/anomalies', label: 'Anomalies', icon: 'warning' },
    { path: '/terminal-health', label: 'Terminals', icon: 'monitor_heart' },
    { path: '/depot-assignments', label: 'Depots', icon: 'store' },
    { path: '/reports', label: 'Reports', icon: 'description' },
    { path: '/audit-logs', label: 'Audit logs', icon: 'history' },
  ];

  logout(): void {
    this.auth.logout();
  }
}

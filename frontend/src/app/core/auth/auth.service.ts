import { Injectable, computed, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { tap } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { LoginRequest, LoginResponse, Role, SessionUser } from '../models/api.models';

const STORAGE_KEY = 'trap.session';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly session = signal<SessionUser | null>(this.readStored());

  readonly user = this.session.asReadonly();
  readonly isAuthenticated = computed(() => !!this.session());
  readonly role = computed(() => this.session()?.role ?? null);
  readonly username = computed(() => this.session()?.username ?? null);

  constructor(private readonly http: HttpClient, private readonly router: Router) {}

  login(credentials: LoginRequest) {
    return this.http.post<LoginResponse>(`${environment.apiBaseUrl}/auth/login`, credentials).pipe(
      tap((res) => {
        const user: SessionUser = {
          username: res.username,
          role: res.role,
          accessToken: res.accessToken,
        };
        localStorage.setItem(STORAGE_KEY, JSON.stringify(user));
        this.session.set(user);
      }),
    );
  }

  logout(): void {
    localStorage.removeItem(STORAGE_KEY);
    this.session.set(null);
    void this.router.navigate(['/login']);
  }

  token(): string | null {
    return this.session()?.accessToken ?? null;
  }

  hasAnyRole(...roles: Role[]): boolean {
    const current = this.role();
    return !!current && roles.includes(current);
  }

  private readStored(): SessionUser | null {
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      if (!raw) {
        return null;
      }
      return JSON.parse(raw) as SessionUser;
    } catch {
      return null;
    }
  }
}

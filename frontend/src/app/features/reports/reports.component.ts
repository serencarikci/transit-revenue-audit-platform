import { Component, inject, OnDestroy, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { Subscription, switchMap, timer } from 'rxjs';
import { ReportingApi } from '../../core/api/api.services';
import { ReportSnapshot } from '../../core/models/api.models';

@Component({
  selector: 'app-reports',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatProgressBarModule,
  ],
  templateUrl: './reports.component.html',
  styleUrl: './reports.component.scss',
})
export class ReportsComponent implements OnDestroy {
  private readonly fb = inject(FormBuilder);
  readonly job = signal<ReportSnapshot | null>(null);
  readonly error = signal<string | null>(null);
  private pollSub?: Subscription;

  readonly form = this.fb.nonNullable.group({
    reportType: ['DEPOT_DAY', Validators.required],
    from: ['', Validators.required],
    to: ['', Validators.required],
  });

  constructor(private readonly api: ReportingApi) {}

  ngOnDestroy(): void {
    this.pollSub?.unsubscribe();
  }

  start(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.error.set(null);
    const v = this.form.getRawValue();
    this.api
      .start({
        reportType: v.reportType,
        from: new Date(v.from).toISOString(),
        to: new Date(v.to).toISOString(),
      })
      .subscribe({
        next: (snap) => {
          this.job.set(snap);
          this.startPolling(snap.id);
        },
        error: (e) => this.error.set(e?.error?.detail ?? e?.error?.message ?? 'Start report failed'),
      });
  }

  download(): void {
    const j = this.job();
    if (!j || j.status !== 'COMPLETED') return;
    this.api.download(j.id).subscribe((blob) => {
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `report-${j.id}.csv`;
      a.click();
      URL.revokeObjectURL(url);
    });
  }

  isRunning(): boolean {
    return this.job()?.status === 'RUNNING';
  }

  private startPolling(id: number): void {
    this.pollSub?.unsubscribe();
    this.pollSub = timer(0, 1500)
      .pipe(switchMap(() => this.api.get(id)))
      .subscribe((snap) => {
        this.job.set(snap);
        if (snap.status === 'COMPLETED' || snap.status === 'FAILED') {
          this.pollSub?.unsubscribe();
        }
      });
  }
}

import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatTableModule } from '@angular/material/table';
import { DepotApi, ReconciliationApi } from '../../core/api/api.services';
import { Depot, ReconciliationResult } from '../../core/models/api.models';

@Component({
  selector: 'app-reconciliation',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatTableModule,
  ],
  templateUrl: './reconciliation.component.html',
  styleUrl: './reconciliation.component.scss',
})
export class ReconciliationComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  readonly depots = signal<Depot[]>([]);
  readonly results = signal<ReconciliationResult[]>([]);
  readonly message = signal<string | null>(null);
  readonly columns = ['id', 'period', 'variance', 'net', 'status', 'actions'];

  readonly form = this.fb.nonNullable.group({
    depotId: [0 as number, Validators.required],
    periodDate: ['', Validators.required],
    openingBalance: [0, Validators.required],
    depositedAmount: [0, Validators.required],
    withdrawalAmount: [0, Validators.required],
    actualClosingBalance: [0, Validators.required],
  });

  constructor(private readonly depotApi: DepotApi,
    private readonly reconciliationApi: ReconciliationApi,
  ) {}

  ngOnInit(): void {
    this.depotApi.list(true).subscribe((page) => this.depots.set(page.content ?? []));
    this.reload();
  }

  reload(): void {
    this.reconciliationApi.results().subscribe((page) => this.results.set(page.content ?? []));
  }

  createAndRun(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const body = this.form.getRawValue();
    this.reconciliationApi.createPeriod(body).subscribe({
      next: (period) => {
        this.reconciliationApi.calculate(period.id).subscribe({
          next: () => {
            this.message.set(`Period ${period.id} reconciled`);
            this.reload();
          },
          error: (err) => this.message.set(err?.error?.detail ?? err?.error?.message ?? 'Calculate failed'),
        });
      },
      error: (err) => this.message.set(err?.error?.detail ?? err?.error?.message ?? 'Create failed'),
    });
  }
}

import { Component, inject, OnInit, signal, ViewChild, ElementRef, AfterViewInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { Chart, registerables } from 'chart.js';
import { ReconciliationApi } from '../../core/api/api.services';
import { ReconciliationResult } from '../../core/models/api.models';

Chart.register(...registerables);

@Component({
  selector: 'app-variance-detail',
  standalone: true,
  imports: [ReactiveFormsModule, MatCardModule, MatFormFieldModule, MatInputModule, MatButtonModule],
  templateUrl: './variance-detail.component.html',
  styleUrl: './variance-detail.component.scss',
})
export class VarianceDetailComponent implements OnInit, AfterViewInit {
  private readonly fb = inject(FormBuilder);
  @ViewChild('chart') chartRef?: ElementRef<HTMLCanvasElement>;
  readonly result = signal<ReconciliationResult | null>(null);
  readonly message = signal<string | null>(null);

  readonly form = this.fb.nonNullable.group({
    resolutionNote: ['', [Validators.required, Validators.maxLength(1024)]],
  });

  constructor(private readonly route: ActivatedRoute,
    private readonly api: ReconciliationApi,
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.api.results().subscribe((page) => {
      const found = (page.content ?? []).find((r) => r.id === id) ?? null;
      this.result.set(found);
      setTimeout(() => this.render(), 0);
    });
  }

  ngAfterViewInit(): void {
    this.render();
  }

  resolve(): void {
    const r = this.result();
    if (!r || this.form.invalid) return;
    this.api.resolve(r.id, { resolutionNote: this.form.value.resolutionNote!, version: r.version }).subscribe({
      next: (updated) => {
        this.result.set(updated);
        this.message.set('Resolved');
      },
      error: (err) => this.message.set(err?.error?.detail ?? err?.error?.message ?? 'Resolve failed'),
    });
  }

  private render(): void {
    const r = this.result();
    if (!r || !this.chartRef) return;
    new Chart(this.chartRef.nativeElement, {
      type: 'bar',
      data: {
        labels: ['Expected', 'Actual', 'Sale', 'Cancel', 'Net', 'Variance'],
        datasets: [
          {
            data: [
              Number(r.expectedClosingBalance),
              Number(r.actualClosingBalance),
              Number(r.saleAmount),
              Number(r.cancellationAmount),
              Number(r.netAmount),
              Number(r.variance),
            ],
            backgroundColor: ['#3b6fd4', '#6b8fdc', '#b8a6e8', '#94a3b8', '#64748b', '#c2410c'],
          },
        ],
      },
      options: { plugins: { legend: { display: false } } },
    });
  }
}

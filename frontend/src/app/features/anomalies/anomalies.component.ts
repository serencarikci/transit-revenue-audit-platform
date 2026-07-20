import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatTableModule } from '@angular/material/table';
import { AnomalyApi } from '../../core/api/api.services';
import { Anomaly } from '../../core/models/api.models';

@Component({
  selector: 'app-anomalies',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatTableModule,
  ],
  templateUrl: './anomalies.component.html',
  styleUrl: './anomalies.component.scss',
})
export class AnomaliesComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  readonly rows = signal<Anomaly[]>([]);
  readonly columns = ['id', 'rule', 'severity', 'status', 'title', 'actions'];
  readonly filters = this.fb.nonNullable.group({ severity: [''], status: [''] });
  readonly resolveForm = this.fb.nonNullable.group({
    id: [0, Validators.required],
    version: [0, Validators.required],
    resolutionNote: ['', Validators.required],
  });

  constructor(private readonly api: AnomalyApi) {}

  ngOnInit(): void {
    this.reload();
  }

  reload(): void {
    const f = this.filters.getRawValue();
    this.api
      .search({ severity: f.severity || undefined, status: f.status || undefined })
      .subscribe((rows) => this.rows.set(rows));
  }

  review(row: Anomaly): void {
    this.api.review(row.id).subscribe(() => this.reload());
  }

  prepareResolve(row: Anomaly): void {
    this.resolveForm.setValue({ id: row.id, version: row.version, resolutionNote: '' });
  }

  resolve(): void {
    if (this.resolveForm.invalid) return;
    const v = this.resolveForm.getRawValue();
    this.api.resolve(v.id, { resolutionNote: v.resolutionNote, version: v.version }).subscribe(() => {
      this.resolveForm.reset({ id: 0, version: 0, resolutionNote: '' });
      this.reload();
    });
  }
}

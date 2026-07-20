import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatTableModule } from '@angular/material/table';
import { AuditLogApi } from '../../core/api/api.services';
import { AuditLog } from '../../core/models/api.models';

@Component({
  selector: 'app-audit-logs',
  standalone: true,
  imports: [ReactiveFormsModule, MatCardModule, MatFormFieldModule, MatInputModule, MatButtonModule, MatTableModule],
  templateUrl: './audit-logs.component.html',
  styleUrl: './audit-logs.component.scss',
})
export class AuditLogsComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  readonly rows = signal<AuditLog[]>([]);
  readonly columns = ['id', 'action', 'entity', 'actor', 'created'];
  readonly form = this.fb.nonNullable.group({ actor: [''], entityType: [''] });

  constructor(private readonly api: AuditLogApi) {}

  ngOnInit(): void {
    this.search();
  }

  search(): void {
    const v = this.form.getRawValue();
    if (v.actor || v.entityType) {
      this.api.search(v.entityType || undefined, v.actor || undefined).subscribe((rows) => this.rows.set(rows));
    } else {
      this.api.list().subscribe((page) => this.rows.set(page.content ?? []));
    }
  }
}

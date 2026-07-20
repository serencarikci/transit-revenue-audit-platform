import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatTableModule } from '@angular/material/table';
import { TransactionApi } from '../../core/api/api.services';
import { Transaction } from '../../core/models/api.models';

@Component({
  selector: 'app-transactions',
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
  templateUrl: './transactions.component.html',
  styleUrl: './transactions.component.scss',
})
export class TransactionsComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  readonly rows = signal<Transaction[]>([]);
  readonly columns = ['id', 'ref', 'terminal', 'type', 'amount', 'time', 'card'];

  readonly form = this.fb.nonNullable.group({
    terminalId: [''],
    type: [''],
    from: [''],
    to: [''],
  });

  constructor(private readonly api: TransactionApi) {}

  ngOnInit(): void {
    this.search();
  }

  search(): void {
    const v = this.form.getRawValue();
    this.api
      .search({
        terminalId: v.terminalId ? Number(v.terminalId) : undefined,
        type: v.type || undefined,
        from: v.from ? new Date(v.from).toISOString() : undefined,
        to: v.to ? new Date(v.to).toISOString() : undefined,
      })
      .subscribe((page) => this.rows.set(page.content ?? []));
  }
}

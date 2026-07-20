import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatTableModule } from '@angular/material/table';
import { DepotApi, TerminalApi } from '../../core/api/api.services';
import { Assignment, Depot, Terminal } from '../../core/models/api.models';

@Component({
  selector: 'app-depot-assignments',
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
  templateUrl: './depot-assignments.component.html',
  styleUrl: './depot-assignments.component.scss',
})
export class DepotAssignmentsComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  readonly depots = signal<Depot[]>([]);
  readonly terminals = signal<Terminal[]>([]);
  readonly assignments = signal<Assignment[]>([]);
  readonly message = signal<string | null>(null);
  readonly columns = ['id', 'terminal', 'depot', 'from', 'to'];

  readonly depotForm = this.fb.nonNullable.group({
    code: ['', [Validators.required, Validators.pattern(/^[A-Z0-9_-]+$/)]],
    name: ['', Validators.required],
  });

  readonly terminalForm = this.fb.nonNullable.group({
    terminalNumber: ['', [Validators.required, Validators.pattern(/^[A-Z0-9_-]+$/)]],
    serialNumber: ['', Validators.required],
  });

  readonly assignmentForm = this.fb.nonNullable.group({
    terminalId: [0, Validators.required],
    depotId: [0, Validators.required],
    validFrom: ['', Validators.required],
  });

  constructor(private readonly depotApi: DepotApi,
    private readonly terminalApi: TerminalApi,
  ) {}

  ngOnInit(): void {
    this.reload();
  }

  reload(): void {
    this.depotApi.list().subscribe((p) => this.depots.set(p.content ?? []));
    this.terminalApi.list().subscribe((p) => {
      const list = p.content ?? [];
      this.terminals.set(list);
      if (list[0]) {
        this.loadAssignments(list[0].id);
      }
    });
  }

  loadAssignments(terminalId: number): void {
    this.terminalApi.assignments(terminalId).subscribe((rows) => this.assignments.set(rows));
  }

  createDepot(): void {
    if (this.depotForm.invalid) return;
    this.depotApi.create(this.depotForm.getRawValue()).subscribe({
      next: () => {
        this.message.set('Depot created');
        this.depotForm.reset();
        this.reload();
      },
      error: (e) => this.message.set(e?.error?.detail ?? e?.error?.message ?? 'Create depot failed'),
    });
  }

  createTerminal(): void {
    if (this.terminalForm.invalid) return;
    this.terminalApi.create(this.terminalForm.getRawValue()).subscribe({
      next: () => {
        this.message.set('Terminal created');
        this.terminalForm.reset();
        this.reload();
      },
      error: (e) => this.message.set(e?.error?.detail ?? e?.error?.message ?? 'Create terminal failed'),
    });
  }

  createAssignment(): void {
    if (this.assignmentForm.invalid) return;
    const v = this.assignmentForm.getRawValue();
    this.terminalApi.createAssignment(v.terminalId, { depotId: v.depotId, validFrom: v.validFrom }).subscribe({
      next: () => {
        this.message.set('Assignment created');
        this.loadAssignments(v.terminalId);
      },
      error: (e) => this.message.set(e?.error?.detail ?? e?.error?.message ?? 'Create assignment failed'),
    });
  }
}

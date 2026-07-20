import { AfterViewInit, Component, ElementRef, OnInit, ViewChild, signal } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { Chart, registerables } from 'chart.js';
import { TerminalApi } from '../../core/api/api.services';
import { Terminal } from '../../core/models/api.models';

Chart.register(...registerables);

@Component({
  selector: 'app-terminal-health',
  standalone: true,
  imports: [MatCardModule, MatTableModule],
  templateUrl: './terminal-health.component.html',
  styleUrl: './terminal-health.component.scss',
})
export class TerminalHealthComponent implements OnInit, AfterViewInit {
  @ViewChild('chart') chartRef?: ElementRef<HTMLCanvasElement>;
  readonly rows = signal<Terminal[]>([]);
  readonly columns = ['number', 'status', 'sync', 'pending', 'retry', 'active'];
  private ok = 0;
  private stale = 0;
  private inactive = 0;

  constructor(private readonly api: TerminalApi) {}

  ngOnInit(): void {
    this.api.list().subscribe((page) => {
      const list = page.content ?? [];
      this.rows.set(list);
      const dayAgo = Date.now() - 24 * 3600 * 1000;
      this.inactive = list.filter((t) => !t.active).length;
      this.stale = list.filter(
        (t) => t.active && (!t.lastSyncTime || new Date(t.lastSyncTime).getTime() < dayAgo),
      ).length;
      this.ok = list.filter(
        (t) => t.active && t.lastSyncTime && new Date(t.lastSyncTime).getTime() >= dayAgo,
      ).length;
      setTimeout(() => this.render(), 0);
    });
  }

  ngAfterViewInit(): void {
    this.render();
  }

  private render(): void {
    if (!this.chartRef) return;
    new Chart(this.chartRef.nativeElement, {
      type: 'doughnut',
      data: {
        labels: ['Healthy', 'Stale sync', 'Inactive'],
        datasets: [{ data: [this.ok, this.stale, this.inactive], backgroundColor: ['#3b6fd4', '#b8a6e8', '#94a3b8'] }],
      },
    });
  }
}

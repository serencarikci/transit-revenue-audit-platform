import { Component, OnInit, ViewChild, ElementRef, AfterViewInit } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { Chart, registerables } from 'chart.js';
import { forkJoin } from 'rxjs';
import { AnomalyApi, ReconciliationApi, TerminalApi } from '../../core/api/api.services';

Chart.register(...registerables);

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [MatCardModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss',
})
export class DashboardComponent implements OnInit, AfterViewInit {
  @ViewChild('varianceChart') varianceChart?: ElementRef<HTMLCanvasElement>;
  @ViewChild('anomalyChart') anomalyChart?: ElementRef<HTMLCanvasElement>;

  openVariances = 0;
  openAnomalies = 0;
  activeTerminals = 0;
  staleTerminals = 0;

  private varianceData: number[] = [];
  private severityCounts = { HIGH: 0, MEDIUM: 0, LOW: 0 };

  constructor(
    private readonly reconciliationApi: ReconciliationApi,
    private readonly anomalyApi: AnomalyApi,
    private readonly terminalApi: TerminalApi,
  ) {}

  ngOnInit(): void {
    forkJoin({
      results: this.reconciliationApi.results(),
      anomalies: this.anomalyApi.search(),
      terminals: this.terminalApi.list(),
    }).subscribe(({ results, anomalies, terminals }) => {
      const open = (results.content ?? []).filter(
        (r) => r.status === 'SMALL_VARIANCE' || r.status === 'LARGE_VARIANCE',
      );
      this.openVariances = open.length;
      this.varianceData = open.slice(0, 12).map((r) => Number(r.variance));
      const openA = anomalies.filter((a) => a.status !== 'RESOLVED');
      this.openAnomalies = openA.length;
      this.severityCounts = { HIGH: 0, MEDIUM: 0, LOW: 0 };
      for (const a of openA) {
        if (a.severity in this.severityCounts) {
          this.severityCounts[a.severity as keyof typeof this.severityCounts]++;
        }
      }
      const list = terminals.content ?? [];
      this.activeTerminals = list.filter((t) => t.active).length;
      const dayAgo = Date.now() - 24 * 3600 * 1000;
      this.staleTerminals = list.filter(
        (t) => t.active && (!t.lastSyncTime || new Date(t.lastSyncTime).getTime() < dayAgo),
      ).length;
      this.renderCharts();
    });
  }

  ngAfterViewInit(): void {
    this.renderCharts();
  }

  private renderCharts(): void {
    if (this.varianceChart) {
      new Chart(this.varianceChart.nativeElement, {
        type: 'bar',
        data: {
          labels: this.varianceData.map((_, i) => `V${i + 1}`),
          datasets: [{ label: 'Variance', data: this.varianceData, backgroundColor: '#3b6fd4' }],
        },
        options: { responsive: true, plugins: { legend: { display: false } } },
      });
    }
    if (this.anomalyChart) {
      new Chart(this.anomalyChart.nativeElement, {
        type: 'doughnut',
        data: {
          labels: ['HIGH', 'MEDIUM', 'LOW'],
          datasets: [
            {
              data: [this.severityCounts.HIGH, this.severityCounts.MEDIUM, this.severityCounts.LOW],
              backgroundColor: ['#3b6fd4', '#b8a6e8', '#94a3b8'],
            },
          ],
        },
        options: { responsive: true },
      });
    }
  }
}

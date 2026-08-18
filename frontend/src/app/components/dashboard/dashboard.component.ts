import { CommonModule } from '@angular/common';
import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { RiskLimitService } from '../../services/risk-limit.service';
import { LimitType, RiskLimit, SectorExposure } from '../../models/risk-limit.model';

type SortField = 'counterpartyName' | 'limitType' | 'sector' | 'maxAmount' | 'usedAmount' | 'usageRate' | 'alertLevel';
type SortDirection = 'asc' | 'desc';

interface SortRule {
  field: SortField;
  direction: SortDirection;
}

/** Chaine de tri par defaut demandee dans le cahier des charges. */
const DEFAULT_SORT_CHAIN: SortRule[] = [
  { field: 'counterpartyName', direction: 'asc' },
  { field: 'limitType', direction: 'asc' },
  { field: 'sector', direction: 'asc' },
  { field: 'maxAmount', direction: 'desc' },
  { field: 'usedAmount', direction: 'desc' },
  { field: 'usageRate', direction: 'desc' },
  { field: 'alertLevel', direction: 'asc' }
];

const PAGE_SIZE = 10;

@Component({
    selector: 'app-dashboard',
    imports: [CommonModule, FormsModule],
    templateUrl: './dashboard.component.html',
    changeDetection: ChangeDetectionStrategy.Eager,
    styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {

  allRiskLimits: RiskLimit[] = [];
  filteredRiskLimits: RiskLimit[] = [];
  pagedRiskLimits: RiskLimit[] = [];

  nameFilter = '';
  currentPage = 1;
  pageSize = PAGE_SIZE;

  /** null = pas de tri utilisateur explicite -> on utilise la chaine par defaut. */
  userSort: SortRule | null = null;

  selectedLimitType: LimitType | 'ALL' = 'ALL';
  sectorExposures: SectorExposure[] = [];
  loadingExposure = false;

  loading = false;
  error: string | null = null;

  constructor(private riskLimitService: RiskLimitService) {}

  ngOnInit(): void {
    this.loadRiskLimits();
  }

  loadRiskLimits(): void {
    this.loading = true;
    this.error = null;
    this.riskLimitService.getAllRiskLimits().subscribe({
      next: (data) => {
        this.allRiskLimits = data;
        this.applyFilterAndSort();
        this.loading = false;
      },
      error: () => {
        this.error = "Impossible de charger les limites de risque. Verifiez que le backend est demarre.";
        this.loading = false;
      }
    });
  }

  onFilterChange(): void {
    this.currentPage = 1;
    this.applyFilterAndSort();
  }

  applyFilterAndSort(): void {
    const filterValue = this.nameFilter.trim().toLowerCase();

    this.filteredRiskLimits = this.allRiskLimits.filter((rl) =>
      rl.counterpartyName.toLowerCase().includes(filterValue)
    );

    this.filteredRiskLimits.sort((a, b) => this.compareByChain(a, b));
    this.goToPage(this.currentPage);
  }

  private compareByChain(a: RiskLimit, b: RiskLimit): number {
    const chain: SortRule[] = this.userSort
      ? [this.userSort, ...DEFAULT_SORT_CHAIN.filter((r) => r.field !== this.userSort!.field)]
      : DEFAULT_SORT_CHAIN;

    for (const rule of chain) {
      const cmp = this.compareField(a, b, rule.field);
      if (cmp !== 0) {
        return rule.direction === 'asc' ? cmp : -cmp;
      }
    }
    return 0;
  }

  private compareField(a: RiskLimit, b: RiskLimit, field: SortField): number {
    const va = a[field];
    const vb = b[field];
    if (typeof va === 'number' && typeof vb === 'number') {
      return va - vb;
    }
    return String(va).localeCompare(String(vb));
  }

  onSortColumn(field: SortField): void {
    if (this.userSort && this.userSort.field === field) {
      this.userSort = { field, direction: this.userSort.direction === 'asc' ? 'desc' : 'asc' };
    } else {
      this.userSort = { field, direction: 'asc' };
    }
    this.applyFilterAndSort();
  }

  sortIndicator(field: SortField): string {
    if (this.userSort && this.userSort.field === field) {
      return this.userSort.direction === 'asc' ? '▲' : '▼';
    }
    return '';
  }

  get totalPages(): number {
    return Math.max(1, Math.ceil(this.filteredRiskLimits.length / this.pageSize));
  }

  goToPage(page: number): void {
    this.currentPage = Math.min(Math.max(1, page), this.totalPages);
    const start = (this.currentPage - 1) * this.pageSize;
    this.pagedRiskLimits = this.filteredRiskLimits.slice(start, start + this.pageSize);
  }

  badgeClass(alertLevel: string): string {
    switch (alertLevel) {
      case 'GREEN': return 'badge badge-green';
      case 'ORANGE': return 'badge badge-orange';
      case 'RED': return 'badge badge-red';
      default: return 'badge';
    }
  }

  onLimitTypeChange(): void {
    if (this.selectedLimitType === 'ALL') {
      this.sectorExposures = [];
      return;
    }
    this.loadingExposure = true;
    this.riskLimitService.getExposureBySector(this.selectedLimitType).subscribe({
      next: (data) => {
        this.sectorExposures = data;
        this.loadingExposure = false;
      },
      error: () => {
        this.error = "Impossible de charger l'exposition agregee par secteur.";
        this.loadingExposure = false;
      }
    });
  }
}

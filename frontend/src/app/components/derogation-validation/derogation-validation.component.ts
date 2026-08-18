import { CommonModule } from '@angular/common';
import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';

import { DerogationService } from '../../services/derogation.service';
import { DerogationRequest } from '../../models/derogation.model';

@Component({
    selector: 'app-derogation-validation',
    imports: [CommonModule],
    templateUrl: './derogation-validation.component.html',
    changeDetection: ChangeDetectionStrategy.Eager,
    styleUrl: './derogation-validation.component.css'
})
export class DerogationValidationComponent implements OnInit {

  pendingRequests: DerogationRequest[] = [];
  loading = false;
  error: string | null = null;
  processingId: number | null = null;

  constructor(private derogationService: DerogationService) {}

  ngOnInit(): void {
    this.loadPending();
  }

  loadPending(): void {
    this.loading = true;
    this.error = null;
    this.derogationService.getPending().subscribe({
      next: (data) => {
        this.pendingRequests = data;
        this.loading = false;
      },
      error: () => {
        this.error = 'Impossible de charger les demandes en attente.';
        this.loading = false;
      }
    });
  }

  approve(request: DerogationRequest): void {
    this.processingId = request.id;
    this.derogationService.approve(request.id).subscribe({
      next: () => {
        this.pendingRequests = this.pendingRequests.filter((r) => r.id !== request.id);
        this.processingId = null;
      },
      error: () => {
        this.error = "Impossible de valider la demande.";
        this.processingId = null;
      }
    });
  }

  reject(request: DerogationRequest): void {
    this.processingId = request.id;
    this.derogationService.reject(request.id).subscribe({
      next: () => {
        this.pendingRequests = this.pendingRequests.filter((r) => r.id !== request.id);
        this.processingId = null;
      },
      error: () => {
        this.error = "Impossible de rejeter la demande.";
        this.processingId = null;
      }
    });
  }
}

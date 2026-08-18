
import { Component, ChangeDetectionStrategy } from '@angular/core';

import { RiskLimitService } from '../../services/risk-limit.service';
import { ImportResult } from '../../models/import-result.model';

@Component({
    selector: 'app-csv-upload',
    imports: [],
    templateUrl: './csv-upload.component.html',
    changeDetection: ChangeDetectionStrategy.Eager,
    styleUrl: './csv-upload.component.css'
})
export class CsvUploadComponent {

  selectedFile: File | null = null;
  uploading = false;
  result: ImportResult | null = null;
  error: string | null = null;

  constructor(private riskLimitService: RiskLimitService) {}

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.selectedFile = input.files && input.files.length > 0 ? input.files[0] : null;
    this.result = null;
    this.error = null;
  }

  upload(): void {
    if (!this.selectedFile) {
      return;
    }
    this.uploading = true;
    this.error = null;
    this.result = null;

    this.riskLimitService.importCsv(this.selectedFile).subscribe({
      next: (result) => {
        this.result = result;
        this.uploading = false;
      },
      error: () => {
        this.error = "Une erreur est survenue pendant l'import du fichier.";
        this.uploading = false;
      }
    });
  }
}

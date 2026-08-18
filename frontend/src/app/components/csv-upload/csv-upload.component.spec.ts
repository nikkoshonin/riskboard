import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Subject, of, throwError } from 'rxjs';

import { CsvUploadComponent } from './csv-upload.component';
import { RiskLimitService } from '../../services/risk-limit.service';
import { ImportResult } from '../../models/import-result.model';

describe('CsvUploadComponent', () => {
  let component: CsvUploadComponent;
  let fixture: ComponentFixture<CsvUploadComponent>;
  let riskLimitService: jasmine.SpyObj<RiskLimitService>;

  beforeEach(async () => {
    const serviceSpy = jasmine.createSpyObj<RiskLimitService>(
      'RiskLimitService',
      ['importCsv']
    );

    await TestBed.configureTestingModule({
      imports: [CsvUploadComponent],
      providers: [
        {
          provide: RiskLimitService,
          useValue: serviceSpy
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(CsvUploadComponent);
    component = fixture.componentInstance;
    riskLimitService = TestBed.inject(
      RiskLimitService
    ) as jasmine.SpyObj<RiskLimitService>;

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('onFileSelected', () => {

    it('should select the file', () => {
      const file = new File(
        ['test'],
        'risk-limits.csv',
        { type: 'text/csv' }
      );

      const event = {
        target: {
          files: [file]
        }
      } as unknown as Event;

      component.onFileSelected(event);

      expect(component.selectedFile).toBe(file);
    });

    it('should reset result and error', () => {
      const file = new File(['test'], 'risk-limits.csv');

      component.result = {
        successCount: 5,
        errors: []
      } as ImportResult;

      component.error = 'Erreur';

      const event = {
        target: {
          files: [file]
        }
      } as unknown as Event;

      component.onFileSelected(event);

      expect(component.result).toBeNull();
      expect(component.error).toBeNull();
    });

    it('should set selectedFile to null when no file is selected', () => {
      const event = {
        target: {
          files: []
        }
      } as unknown as Event;

      component.onFileSelected(event);

      expect(component.selectedFile).toBeNull();
    });
  });

  describe('upload', () => {

    it('should not call the service when no file is selected', () => {
      component.selectedFile = null;

      component.upload();

      expect(riskLimitService.importCsv).not.toHaveBeenCalled();
    });

    it('should call importCsv with the selected file', () => {
      const file = new File(['test'], 'risk-limits.csv');

      component.selectedFile = file;

      riskLimitService.importCsv.and.returnValue(
        of({ successCount: 1, errors: [] } as ImportResult)
      );

      component.upload();

      expect(riskLimitService.importCsv)
        .toHaveBeenCalledWith(file);
    });

    it('should update result when import succeeds', () => {
      const file = new File(['test'], 'risk-limits.csv');

      const result = {
        successCount: 3,
        errors: []
      } as ImportResult;

      component.selectedFile = file;

      riskLimitService.importCsv.and.returnValue(of(result));

      component.upload();

      expect(component.result).toEqual(result);
      expect(component.error).toBeNull();
      expect(component.uploading).toBeFalse();
    });

    it('should handle import error', () => {
      const file = new File(['test'], 'risk-limits.csv');

      component.selectedFile = file;

      riskLimitService.importCsv.and.returnValue(
        throwError(() => new Error('Import failed'))
      );

      component.upload();

      expect(component.result).toBeNull();

      expect(component.error)
        .toBe("Une erreur est survenue pendant l'import du fichier.");

      expect(component.uploading).toBeFalse();
    });

    it('should set uploading to true while the request is pending', () => {
      const file = new File(['test'], 'risk-limits.csv');
      const subject = new Subject<ImportResult>();

      component.selectedFile = file;

      riskLimitService.importCsv.and.returnValue(subject);

      component.upload();

      expect(component.uploading).toBeTrue();

      subject.next({
        successCount: 1,
        errors: []
      } as ImportResult);

      subject.complete();

      expect(component.uploading).toBeFalse();
    });
  });
});
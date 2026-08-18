import { Routes } from '@angular/router';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { CsvUploadComponent } from './components/csv-upload/csv-upload.component';
import { DerogationFormComponent } from './components/derogation-form/derogation-form.component';
import { DerogationValidationComponent } from './components/derogation-validation/derogation-validation.component';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
  { path: 'dashboard', component: DashboardComponent },
  { path: 'import', component: CsvUploadComponent },
  { path: 'derogations/new', component: DerogationFormComponent },
  { path: 'derogations/validation', component: DerogationValidationComponent }
];

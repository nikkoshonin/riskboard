import { AbstractControl, AsyncValidatorFn, ValidationErrors } from '@angular/forms';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';

import { DerogationService } from '../services/derogation.service';
import { LimitType } from '../models/risk-limit.model';

/**
 * Validator asynchrone : verifie aupres du backend que le montant demande ne
 * depasse pas 150% de la limite max existante pour la contrepartie et le
 * type de risque selectionnes. Les valeurs de contrepartie/type sont lues
 * dynamiquement via les callbacks fournis, car elles vivent dans des champs
 * freres du controle 'amount'.
 */
export function maxDerogationAmountValidator(
  derogationService: DerogationService,
  getCounterpartyId: () => (number | null),
  getLimitType: () => (LimitType | null)
): AsyncValidatorFn {
  return (control: AbstractControl): Observable<ValidationErrors | null> => {
    const counterpartyId = getCounterpartyId();
    const limitType = getLimitType();
    const amount = control.value;

    if (!counterpartyId || !limitType || amount === null || amount === undefined || amount === '') {
      return of(null);
    }

    return derogationService.checkLimit(counterpartyId, limitType).pipe(
      map((check) => {
        if (!check.limitExists) {
          // La contrainte "limite existante" est geree separement au niveau
          // du formulaire ; on ne bloque pas le champ montant pour cela.
          return null;
        }
        if (check.maxAllowedDerogationAmount !== null && amount > check.maxAllowedDerogationAmount) {
          return { maxAmountExceeded: { maxAllowed: check.maxAllowedDerogationAmount } };
        }
        return null;
      }),
      catchError(() => of(null))
    );
  };
}

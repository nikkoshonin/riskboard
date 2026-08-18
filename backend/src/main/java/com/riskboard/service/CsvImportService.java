package com.riskboard.service;

import com.riskboard.dto.ImportResultDto;

import java.io.InputStream;

/**
 * Service responsable de l'import des contreparties et de leurs limites
 * à partir d'un fichier CSV.
 *
 * <p>Le fichier CSV doit contenir les colonnes suivantes :</p>
 *
 * <pre>
 * name,ricosCode,country,sector,limitType,maxAmount,usedAmount,currency
 * </pre>
 *
 * <p>Chaque ligne est traitée individuellement. Une erreur sur une ligne
 * n'empêche pas le traitement des lignes suivantes. Les erreurs rencontrées
 * sont retournées dans le {@link ImportResultDto}.</p>
 *
 * <p>Lorsqu'une contrepartie ou une limite existe déjà, les données sont
 * mises à jour. Dans le cas contraire, les entités correspondantes sont
 * créées.</p>
 */
public interface CsvImportService {

    /**
     * Importe les contreparties et leurs limites depuis un fichier CSV.
     *
     * <p>Le fichier doit être encodé en UTF-8 et contenir une ligne
     * d'en-tête correspondant aux colonnes attendues.</p>
     *
     * <p>Les lignes invalides sont ignorées individuellement et les erreurs
     * associées sont ajoutées au résultat de l'import.</p>
     *
     * @param inputStream flux contenant le fichier CSV à importer
     * @return le résultat de l'import contenant le nombre de lignes traitées
     *         avec succès ainsi que les éventuelles erreurs
     * @throws IllegalArgumentException si le flux fourni est invalide
     */
    ImportResultDto importCsv(InputStream inputStream);
}
export class DateUtil {
  private static readonly BR_FORMAT = /^(\d{2})\/(\d{2})\/(\d{4})$/;

  static parseFlexibleDate(value: string | null | undefined): Date | null {
    if (!value) return null;
    const v = value.trim();
    if (v === '') return null;

    // Primeiro tenta ISO (yyyy-MM-dd)
    try {
      const isoDate = new Date(v);
      if (!isNaN(isoDate.getTime())) {
        return isoDate;
      }
    } catch (e) {
      // Continua
    }

    // Depois tenta BR (dd/MM/yyyy)
    const brMatch = v.match(this.BR_FORMAT);
    if (brMatch) {
      const [, day, month, year] = brMatch;
      const brDate = new Date(`${year}-${month}-${day}`);
      if (!isNaN(brDate.getTime())) {
        return brDate;
      }
    }

    throw new Error(
      `Data inválida. Use 'yyyy-MM-dd' ou 'dd/MM/yyyy'. Valor: ${value}`,
    );
  }
}

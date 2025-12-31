export interface DashboardFilterRequest {
  countyCodes?: string[];
  ethnicities?: string[];
  genders?: string[];
  ageGroups?: string[];
  races?: string[];
  agedBlindDisabled?: string[];
  caseTypes?: string[];
  severelyImpaired?: boolean;
  evvStatus?: string;
  dimension1?: string;
  dimension2?: string;
  dimension3?: string;
  dimension4?: string;
  dimension5?: string;
  dimension6?: string;
  dimension7?: string;
  dimension8?: string;
  selectedMeasures?: string[];
  viewType?: 'details' | 'pivot';
}

export interface DashboardTableRow {
  race?: string;
  ethnicity?: string;
  gender?: string;
  ageGroup?: string;
  agedBlindDisabled?: string;
  caseType?: string;
  countyName?: string;
  count: number;
  countyPopulation: number;
  authorizedHours: number;
  perCapitaRate: number;
  applicationsReceived?: number;
  applicationsApproved?: number;
  applicationsDenied?: number;
  activeErrorRate?: number;
}

export interface FilterOptions {
  counties: string[];
  ethnicities: string[];
  genders: string[];
  ageGroups: string[];
  races: string[];
  agedBlindDisabled: string[];
  caseTypes: string[];
  dimensions: string[];
  measures: string[];
}

export interface DashboardResponse {
  totalIndividuals: number;
  totalPopulation: number;
  perCapitaRate: number;
  totalAuthorizedHours: number;
  avgAuthorizedHours: number;
  tableData: DashboardTableRow[];
  filterOptions: FilterOptions;
}

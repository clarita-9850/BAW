import apiClient from '../api';

export interface Person {
  id?: number;
  first_name: string;
  middle_name?: string;
  last_name: string;
  suffix?: string;
  ssn: string;
  date_of_birth?: string;
  gender?: string;
  status?: string;
  [key: string]: any;
}

export interface PersonSearchCriteria {
  firstName?: string;
  lastName?: string;
  ssn?: string;
  dateOfBirth?: string;
  searchType?: string; // "NAME", "SSN", "DOB", "COMBINED"
}

export interface PersonSearchResponse {
  success: boolean;
  results: Person[];
  count: number;
  error?: string;
}

export const personService = {
  /**
   * Search for persons by criteria
   */
  async searchPersons(criteria: PersonSearchCriteria): Promise<PersonSearchResponse> {
    const response = await apiClient.post<PersonSearchResponse>('/person/search', criteria);
    return response.data;
  },

  /**
   * Get person by ID
   */
  async getPersonById(personId: number): Promise<Person> {
    const response = await apiClient.get<Person>(`/person/${personId}`);
    return response.data;
  },

  /**
   * Create a new person record
   */
  async createPerson(person: Partial<Person>): Promise<Person> {
    const response = await apiClient.post<Person>('/person', person);
    return response.data;
  },

  /**
   * Update a person record
   */
  async updatePerson(personId: number, person: Partial<Person>): Promise<Person> {
    const response = await apiClient.put<Person>(`/person/${personId}`, person);
    return response.data;
  },
};


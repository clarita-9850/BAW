import React, { useState, useEffect } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { apiClient, API_ENDPOINTS } from '../config/api';

interface Person {
  id: number;
  personId: string;
  firstName: string;
  lastName: string;
  ssn?: string;
  dateOfBirth?: string;
  address?: string;
  phone?: string;
  email?: string;
  searchCriteria: string;
}

const PersonSearch: React.FC = () => {
  const { user } = useAuth();
  const [persons, setPersons] = useState<Person[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [searchForm, setSearchForm] = useState({
    firstName: '',
    lastName: '',
    ssn: '',
    dateOfBirth: '',
    searchCriteria: 'name'
  });
  const [showForm, setShowForm] = useState(false);
  const [editingPerson, setEditingPerson] = useState<Person | null>(null);
  const [formData, setFormData] = useState({
    personId: '',
    firstName: '',
    lastName: '',
    ssn: '',
    dateOfBirth: '',
    address: '',
    phone: '',
    email: ''
  });

  useEffect(() => {
    // Load initial mock data
    const mockPersons: Person[] = [
      {
        id: 1,
        personId: 'PERSON-001',
        firstName: 'John',
        lastName: 'Doe',
        ssn: '123-45-6789',
        dateOfBirth: '1985-03-15',
        address: '123 Main St, Sacramento, CA 95814',
        phone: '(916) 555-0123',
        email: 'john.doe@email.com',
        searchCriteria: 'name'
      },
      {
        id: 2,
        personId: 'PERSON-002',
        firstName: 'Jane',
        lastName: 'Smith',
        ssn: '987-65-4321',
        dateOfBirth: '1990-07-22',
        address: '456 Oak Ave, Sacramento, CA 95825',
        phone: '(916) 555-0456',
        email: 'jane.smith@email.com',
        searchCriteria: 'name'
      }
    ];
    setPersons(mockPersons);
  }, []);

  const handleSearch = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      // Mock search - in real implementation, this would call the backend
      const filteredPersons = persons.filter(person => {
        if (searchForm.searchCriteria === 'name') {
          return person.firstName.toLowerCase().includes(searchForm.firstName.toLowerCase()) ||
                 person.lastName.toLowerCase().includes(searchForm.lastName.toLowerCase());
        } else if (searchForm.searchCriteria === 'ssn') {
          return person.ssn?.includes(searchForm.ssn);
        } else if (searchForm.searchCriteria === 'dob') {
          return person.dateOfBirth === searchForm.dateOfBirth;
        }
        return true;
      });

      setPersons(filteredPersons);
    } catch (err) {
      setError('Search failed');
      console.error('Error searching persons:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      if (editingPerson) {
        // Update existing person
        const updatedPerson = { ...editingPerson, ...formData };
        setPersons(persons.map(p => p.id === editingPerson.id ? updatedPerson : p));
      } else {
        // Create new person
        const newPerson: Person = {
          id: Date.now(),
          ...formData,
          searchCriteria: 'name'
        };
        setPersons([...persons, newPerson]);
      }

      setShowForm(false);
      setEditingPerson(null);
      setFormData({
        personId: '',
        firstName: '',
        lastName: '',
        ssn: '',
        dateOfBirth: '',
        address: '',
        phone: '',
        email: ''
      });
    } catch (err) {
      setError('Failed to save person');
      console.error('Error saving person:', err);
    }
  };

  const handleEdit = (person: Person) => {
    setEditingPerson(person);
    setFormData({
      personId: person.personId,
      firstName: person.firstName,
      lastName: person.lastName,
      ssn: person.ssn || '',
      dateOfBirth: person.dateOfBirth || '',
      address: person.address || '',
      phone: person.phone || '',
      email: person.email || ''
    });
    setShowForm(true);
  };

  const handleDelete = async (id: number) => {
    if (window.confirm('Are you sure you want to delete this person?')) {
      setPersons(persons.filter(p => p.id !== id));
    }
  };

  const clearSearch = () => {
    setSearchForm({
      firstName: '',
      lastName: '',
      ssn: '',
      dateOfBirth: '',
      searchCriteria: 'name'
    });
    // Reload all persons
    const mockPersons: Person[] = [
      {
        id: 1,
        personId: 'PERSON-001',
        firstName: 'John',
        lastName: 'Doe',
        ssn: '123-45-6789',
        dateOfBirth: '1985-03-15',
        address: '123 Main St, Sacramento, CA 95814',
        phone: '(916) 555-0123',
        email: 'john.doe@email.com',
        searchCriteria: 'name'
      },
      {
        id: 2,
        personId: 'PERSON-002',
        firstName: 'Jane',
        lastName: 'Smith',
        ssn: '987-65-4321',
        dateOfBirth: '1990-07-22',
        address: '456 Oak Ave, Sacramento, CA 95825',
        phone: '(916) 555-0456',
        email: 'jane.smith@email.com',
        searchCriteria: 'name'
      }
    ];
    setPersons(mockPersons);
  };

  return (
    <div className="min-h-screen bg-ca-secondary-50">
      <div className="container py-6">
        <div className="px-4 py-6 sm:px-0">
          <div className="flex justify-between items-center mb-6">
            <h1 className="text-2xl font-bold text-ca-primary-900">Person Search</h1>
            <button
              onClick={() => setShowForm(true)}
              className="btn btn-primary"
            >
              Add New Person
            </button>
          </div>

          {error && (
            <div className="alert alert-error mb-6">
              {error}
            </div>
          )}

          {/* Search Form */}
          <div className="card mb-6">
            <div className="card-header">
              <h2 className="text-lg font-medium text-ca-primary-900">Search Persons</h2>
            </div>
            <div className="card-body">
              <form onSubmit={handleSearch} className="space-y-4">
                <div>
                  <label className="form-label">Search Criteria</label>
                  <select
                    className="input mt-1"
                    value={searchForm.searchCriteria}
                    onChange={(e) => setSearchForm({ ...searchForm, searchCriteria: e.target.value })}
                  >
                    <option value="name">Name</option>
                    <option value="ssn">SSN</option>
                    <option value="dob">Date of Birth</option>
                  </select>
                </div>

                {searchForm.searchCriteria === 'name' && (
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div>
                      <label className="form-label">First Name</label>
                      <input
                        type="text"
                        className="input mt-1"
                        value={searchForm.firstName}
                        onChange={(e) => setSearchForm({ ...searchForm, firstName: e.target.value })}
                      />
                    </div>
                    <div>
                      <label className="form-label">Last Name</label>
                      <input
                        type="text"
                        className="input mt-1"
                        value={searchForm.lastName}
                        onChange={(e) => setSearchForm({ ...searchForm, lastName: e.target.value })}
                      />
                    </div>
                  </div>
                )}

                {searchForm.searchCriteria === 'ssn' && (
                  <div>
                    <label className="form-label">SSN</label>
                    <input
                      type="text"
                      placeholder="123-45-6789"
                      className="input mt-1"
                      value={searchForm.ssn}
                      onChange={(e) => setSearchForm({ ...searchForm, ssn: e.target.value })}
                    />
                  </div>
                )}

                {searchForm.searchCriteria === 'dob' && (
                  <div>
                    <label className="form-label">Date of Birth</label>
                    <input
                      type="date"
                      className="input mt-1"
                      value={searchForm.dateOfBirth}
                      onChange={(e) => setSearchForm({ ...searchForm, dateOfBirth: e.target.value })}
                    />
                  </div>
                )}

                <div className="flex space-x-3">
                  <button
                    type="submit"
                    disabled={loading}
                    className="btn btn-primary"
                  >
                    {loading ? 'Searching...' : 'Search'}
                  </button>
                  <button
                    type="button"
                    onClick={clearSearch}
                    className="btn btn-secondary"
                  >
                    Clear
                  </button>
                </div>
              </form>
            </div>
          </div>

          {/* Person Form */}
          {showForm && (
            <div className="card mb-6">
              <div className="card-header">
                <h2 className="text-lg font-medium text-ca-primary-900">
                  {editingPerson ? 'Edit Person' : 'Add New Person'}
                </h2>
              </div>
              <div className="card-body">
                <form onSubmit={handleSubmit} className="space-y-4">
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div>
                      <label className="form-label">Person ID</label>
                      <input
                        type="text"
                        required
                        className="input mt-1"
                        value={formData.personId}
                        onChange={(e) => setFormData({ ...formData, personId: e.target.value })}
                      />
                    </div>
                    
                    <div>
                      <label className="form-label">First Name</label>
                      <input
                        type="text"
                        required
                        className="input mt-1"
                        value={formData.firstName}
                        onChange={(e) => setFormData({ ...formData, firstName: e.target.value })}
                      />
                    </div>
                  </div>
                  
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div>
                      <label className="form-label">Last Name</label>
                      <input
                        type="text"
                        required
                        className="input mt-1"
                        value={formData.lastName}
                        onChange={(e) => setFormData({ ...formData, lastName: e.target.value })}
                      />
                    </div>
                    
                    <div>
                      <label className="form-label">SSN</label>
                      <input
                        type="text"
                        placeholder="123-45-6789"
                        className="input mt-1"
                        value={formData.ssn}
                        onChange={(e) => setFormData({ ...formData, ssn: e.target.value })}
                      />
                    </div>
                  </div>
                  
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div>
                      <label className="form-label">Date of Birth</label>
                      <input
                        type="date"
                        className="input mt-1"
                        value={formData.dateOfBirth}
                        onChange={(e) => setFormData({ ...formData, dateOfBirth: e.target.value })}
                      />
                    </div>
                    
                    <div>
                      <label className="form-label">Phone</label>
                      <input
                        type="text"
                        placeholder="(916) 555-0123"
                        className="input mt-1"
                        value={formData.phone}
                        onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
                      />
                    </div>
                  </div>
                  
                  <div>
                    <label className="form-label">Address</label>
                    <input
                      type="text"
                      className="input mt-1"
                      value={formData.address}
                      onChange={(e) => setFormData({ ...formData, address: e.target.value })}
                    />
                  </div>
                  
                  <div>
                    <label className="form-label">Email</label>
                    <input
                      type="email"
                      className="input mt-1"
                      value={formData.email}
                      onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                    />
                  </div>
                  
                  <div className="flex justify-end space-x-3">
                    <button
                      type="button"
                      onClick={() => {
                        setShowForm(false);
                        setEditingPerson(null);
                        setFormData({
                          personId: '',
                          firstName: '',
                          lastName: '',
                          ssn: '',
                          dateOfBirth: '',
                          address: '',
                          phone: '',
                          email: ''
                        });
                      }}
                      className="btn btn-secondary"
                    >
                      Cancel
                    </button>
                    <button type="submit" className="btn btn-primary">
                      {editingPerson ? 'Update' : 'Add'} Person
                    </button>
                  </div>
                </form>
              </div>
            </div>
          )}

          {/* Persons List */}
          <div className="card">
            <div className="card-header">
              <h2 className="text-lg font-medium text-ca-primary-900">Search Results</h2>
            </div>
            <div className="card-body">
              {persons.length === 0 ? (
                <div className="text-center py-8 text-ca-primary-500">
                  No persons found. Try adjusting your search criteria.
                </div>
              ) : (
                <div className="overflow-x-auto">
                  <table className="table">
                    <thead>
                      <tr>
                        <th>Person ID</th>
                        <th>Name</th>
                        <th>SSN</th>
                        <th>Date of Birth</th>
                        <th>Phone</th>
                        <th>Email</th>
                        <th>Actions</th>
                      </tr>
                    </thead>
                    <tbody>
                      {persons.map((person) => (
                        <tr key={person.id}>
                          <td>
                            <span className="font-medium text-ca-primary-900">{person.personId}</span>
                          </td>
                          <td>
                            <span className="text-sm text-ca-primary-900">
                              {person.firstName} {person.lastName}
                            </span>
                          </td>
                          <td>
                            <span className="text-sm text-ca-primary-600">{person.ssn || '-'}</span>
                          </td>
                          <td>
                            <span className="text-sm text-ca-primary-600">{person.dateOfBirth || '-'}</span>
                          </td>
                          <td>
                            <span className="text-sm text-ca-primary-600">{person.phone || '-'}</span>
                          </td>
                          <td>
                            <span className="text-sm text-ca-primary-600">{person.email || '-'}</span>
                          </td>
                          <td>
                            <div className="flex space-x-2">
                              <button
                                onClick={() => handleEdit(person)}
                                className="text-ca-highlight-600 hover:text-ca-highlight-900 text-sm font-medium"
                              >
                                Edit
                              </button>
                              <button
                                onClick={() => handleDelete(person.id)}
                                className="text-bs-danger hover:text-red-900 text-sm font-medium"
                              >
                                Delete
                              </button>
                            </div>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default PersonSearch;



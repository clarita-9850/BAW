'use client';

import React, { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import Breadcrumb from '@/components/Breadcrumb';
import { useAuth } from '@/contexts/AuthContext';
import styles from './new-application.module.css';

export default function NewApplicationPage() {
  const router = useRouter();
  const { isAuthenticated, loading: authLoading } = useAuth();
  const breadcrumbPath = ['Home', 'My Workspace'];
  const currentPage = 'Person Search';

  // Helper function to calculate age group from date of birth
  const calculateAgeGroup = (dateOfBirth: string): string => {
    if (!dateOfBirth) return '';
    const birthDate = new Date(dateOfBirth);
    const today = new Date();
    const age = today.getFullYear() - birthDate.getFullYear();
    const monthDiff = today.getMonth() - birthDate.getMonth();
    if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthDate.getDate())) {
      const adjustedAge = age - 1;
      if (adjustedAge < 18) return '';
      if (adjustedAge >= 18 && adjustedAge <= 24) return '18-24';
      if (adjustedAge >= 25 && adjustedAge <= 34) return '25-34';
      if (adjustedAge >= 35 && adjustedAge <= 44) return '35-44';
      if (adjustedAge >= 45 && adjustedAge <= 54) return '45-54';
      if (adjustedAge >= 55 && adjustedAge <= 64) return '55-64';
      return '65+';
    }
    if (age < 18) return '';
    if (age >= 18 && age <= 24) return '18-24';
    if (age >= 25 && age <= 34) return '25-34';
    if (age >= 35 && age <= 44) return '35-44';
    if (age >= 45 && age <= 54) return '45-54';
    if (age >= 55 && age <= 64) return '55-64';
    return '65+';
  };

  // Create Application modal state
  const [showCreateApplicationModal, setShowCreateApplicationModal] = useState(false);
  const [currentStep, setCurrentStep] = useState(1);
  const [applicationForm, setApplicationForm] = useState({
    // Personal Information
    appFirstName: '',
    appLastName: '',
    appMiddleName: '',
    appSuffix: '',
    ssn: '',
    dateOfBirth: '',
    gender: '',
    ethnicity: '',
    ageGroup: '', // Calculated from date of birth or selected
    // Contact Information
    primaryPhone: '',
    secondaryPhone: '',
    emailAddress: '',
    preferredSpokenLanguage: '',
    preferredWrittenLanguage: '',
    // Residence Address (Required)
    residenceAddressLine1: '',
    residenceAddressLine2: '',
    residenceCity: '',
    residenceState: 'CA',
    residenceZip: '',
    // Mailing Address
    mailingSameAsResidence: true,
    mailingAddressLine1: '',
    mailingAddressLine2: '',
    mailingCity: '',
    mailingState: '',
    mailingZip: '',
    // Location
    countyOfResidence: '',
    // Guardian/Conservator (Optional)
    guardianConservatorName: '',
    guardianConservatorAddress: '',
    guardianConservatorPhone: '',
    // Disaster Preparedness
    disasterPreparednessCode: '',
    // Additional
    additionalInfo: '',
  });

  const [caseForm, setCaseForm] = useState({
    ihssReferralData: '',
    interpreterAvailable: false,
    assignedWorker: '',
    clientIndexNumber: '',
  });

  const [ssnVerificationStatus, setSsnVerificationStatus] = useState<'idle' | 'success' | 'error'>('idle');
  const [ssnVerificationMessage, setSsnVerificationMessage] = useState('');
  const [verifyingSsn, setVerifyingSsn] = useState(false);

  // Person Search state
  const [searchResults, setSearchResults] = useState<any[]>([]);
  const [searchLoading, setSearchLoading] = useState(false);
  const [searchError, setSearchError] = useState<string | null>(null);
  const [showResults, setShowResults] = useState(false);
  const [searchForm, setSearchForm] = useState({
    lastName: '',
    firstName: '',
    ssn: '',
    cin: '',
    personType: '',
    providerNumber: '',
    dateOfBirth: '',
    gender: '',
    districtOffice: '',
    streetNumber: '',
    streetName: '',
    unitType: '',
    unitNumber: '',
    city: '',
  });

  const openCreateApplicationModal = () => {
    setShowCreateApplicationModal(true);
    setCurrentStep(1);
  };

  const closeCreateApplicationModal = () => {
    setShowCreateApplicationModal(false);
    setCurrentStep(1);
    setApplicationForm({
      appFirstName: '',
      appLastName: '',
      appMiddleName: '',
      appSuffix: '',
      ssn: '',
      dateOfBirth: '',
      gender: '',
      ethnicity: '',
      ageGroup: '',
      primaryPhone: '',
      secondaryPhone: '',
      emailAddress: '',
      preferredSpokenLanguage: '',
      preferredWrittenLanguage: '',
      residenceAddressLine1: '',
      residenceAddressLine2: '',
      residenceCity: '',
      residenceState: 'CA',
      residenceZip: '',
      mailingSameAsResidence: true,
      mailingAddressLine1: '',
      mailingAddressLine2: '',
      mailingCity: '',
      mailingState: '',
      mailingZip: '',
      countyOfResidence: '',
      guardianConservatorName: '',
      guardianConservatorAddress: '',
      guardianConservatorPhone: '',
      disasterPreparednessCode: '',
      additionalInfo: '',
    });
    setCaseForm({
      ihssReferralData: '',
      interpreterAvailable: false,
      assignedWorker: '',
      clientIndexNumber: '',
    });
  };

  const nextStep = () => {
    if (currentStep < 2) {
      setCurrentStep(currentStep + 1);
    }
  };

  const previousStep = () => {
    if (currentStep > 1) {
      setCurrentStep(currentStep - 1);
    }
  };

  const submitCase = async () => {
    try {
      // Validate required fields
      if (!applicationForm.appFirstName || !applicationForm.appLastName) {
        alert('Please fill in First Name and Last Name');
        return;
      }
      if (!applicationForm.ssn) {
        alert('SSN is required');
        return;
      }
      if (!applicationForm.dateOfBirth) {
        alert('Date of Birth is required');
        return;
      }
      if (!applicationForm.residenceAddressLine1) {
        alert('Residence Address Line 1 is required');
        return;
      }
      if (!applicationForm.residenceCity) {
        alert('Residence City is required');
        return;
      }
      if (!applicationForm.residenceZip) {
        alert('Residence ZIP code is required');
        return;
      }
      if (!applicationForm.countyOfResidence) {
        alert('County of Residence is required');
        return;
      }

      const { caseService } = await import('@/lib/services/case.service');
      
      // Build person data for case creation with all fields
      const personData = {
        firstName: applicationForm.appFirstName,
        middleName: applicationForm.appMiddleName || undefined,
        lastName: applicationForm.appLastName,
        suffix: applicationForm.appSuffix || undefined,
        ssn: applicationForm.ssn,
        dateOfBirth: applicationForm.dateOfBirth,
        gender: applicationForm.gender || undefined,
        ethnicity: applicationForm.ethnicity || undefined,
        ageGroup: applicationForm.ageGroup || (applicationForm.dateOfBirth ? calculateAgeGroup(applicationForm.dateOfBirth) : undefined),
        // Recipient demographic fields for timesheet analytics
        recipientGender: applicationForm.gender || undefined,
        recipientEthnicity: applicationForm.ethnicity || undefined,
        recipientAgeGroup: applicationForm.ageGroup || (applicationForm.dateOfBirth ? calculateAgeGroup(applicationForm.dateOfBirth) : undefined),
        recipientDateOfBirth: applicationForm.dateOfBirth || undefined,
        preferredSpokenLanguage: applicationForm.preferredSpokenLanguage || undefined,
        preferredWrittenLanguage: applicationForm.preferredWrittenLanguage || undefined,
        primaryPhone: applicationForm.primaryPhone || undefined,
        secondaryPhone: applicationForm.secondaryPhone || undefined,
        email: applicationForm.emailAddress || undefined,
        residenceAddressLine1: applicationForm.residenceAddressLine1,
        residenceAddressLine2: applicationForm.residenceAddressLine2 || undefined,
        residenceCity: applicationForm.residenceCity,
        residenceState: applicationForm.residenceState || 'CA',
        residenceZip: applicationForm.residenceZip,
        mailingSameAsResidence: applicationForm.mailingSameAsResidence,
        mailingAddressLine1: applicationForm.mailingSameAsResidence ? undefined : (applicationForm.mailingAddressLine1 || undefined),
        mailingAddressLine2: applicationForm.mailingSameAsResidence ? undefined : (applicationForm.mailingAddressLine2 || undefined),
        mailingCity: applicationForm.mailingSameAsResidence ? undefined : (applicationForm.mailingCity || undefined),
        mailingState: applicationForm.mailingSameAsResidence ? undefined : (applicationForm.mailingState || undefined),
        mailingZip: applicationForm.mailingSameAsResidence ? undefined : (applicationForm.mailingZip || undefined),
        countyOfResidence: applicationForm.countyOfResidence,
        guardianConservatorName: applicationForm.guardianConservatorName || undefined,
        guardianConservatorAddress: applicationForm.guardianConservatorAddress || undefined,
        guardianConservatorPhone: applicationForm.guardianConservatorPhone || undefined,
        disasterPreparednessCode: applicationForm.disasterPreparednessCode || undefined,
      };

      const caseRequest = {
        personData: personData,
        countyCode: applicationForm.countyOfResidence,
        caseNotes: caseForm.ihssReferralData || applicationForm.additionalInfo || undefined,
        disasterPreparednessCode: applicationForm.disasterPreparednessCode || undefined,
      };

      const result = await caseService.createCase(caseRequest);

      if (result.success) {
        alert(`Case created successfully! CMIPS Case Number: ${result.cmipsCaseNumber}`);
        closeCreateApplicationModal();
        resetSearch(); // Reset search after successful case creation
      } else {
        throw new Error(result.message || 'Failed to create case');
      }
    } catch (error: any) {
      console.error('Error creating case:', error);
      alert(`Error creating case: ${error.message || 'Please try again.'}`);
    }
  };

  const verifySsn = async () => {
    if (!applicationForm.appFirstName || !applicationForm.ssn) {
      setSsnVerificationStatus('error');
      setSsnVerificationMessage('First name and SSN are required for verification');
      return;
    }

    try {
      setVerifyingSsn(true);
      setSsnVerificationStatus('idle');
      setSsnVerificationMessage('');
      const { caseService } = await import('@/lib/services/case.service');
      const response = await caseService.verifySsn(applicationForm.appFirstName, applicationForm.ssn);
      if (response.valid) {
        setSsnVerificationStatus('success');
        setSsnVerificationMessage(response.message || 'SSN verification successful');
      } else {
        setSsnVerificationStatus('error');
        setSsnVerificationMessage(response.message || 'SSN verification failed');
      }
    } catch (error: any) {
      console.error('SSN verification error:', error);
      setSsnVerificationStatus('error');
      setSsnVerificationMessage(error?.message || 'SSN verification failed');
    } finally {
      setVerifyingSsn(false);
    }
  };

  const performSearch = async () => {
    try {
      setSearchLoading(true);
      setSearchError(null);

      const { personService } = await import('@/lib/services/person.service');
      
      // Build search criteria matching backend format
      const searchCriteria: any = {};
      if (searchForm.lastName) searchCriteria.lastName = searchForm.lastName;
      if (searchForm.firstName) searchCriteria.firstName = searchForm.firstName;
      if (searchForm.ssn) searchCriteria.ssn = searchForm.ssn;
      if (searchForm.dateOfBirth) searchCriteria.dateOfBirth = searchForm.dateOfBirth;
      
      // Determine search type
      if (searchForm.ssn) {
        searchCriteria.searchType = 'SSN';
      } else if (searchForm.firstName || searchForm.lastName) {
        searchCriteria.searchType = 'NAME';
      } else if (searchForm.dateOfBirth) {
        searchCriteria.searchType = 'DOB';
      }

      const hasCriteria = Object.values(searchCriteria).some(
        (value) => value && value.toString().trim() !== ''
      );

      if (!hasCriteria) {
        setSearchError('Please enter at least one search criteria');
        return;
      }

      const result = await personService.searchPersons(searchCriteria);

      if (result.success) {
        // Map backend response to frontend format
        const mappedResults = (result.results || []).map((person: any) => ({
          id: person.personId,
          personId: person.personId, // Keep for case creation
          first_name: person.firstName,
          firstName: person.firstName,
          middle_name: person.middleName,
          middleName: person.middleName,
          last_name: person.lastName,
          lastName: person.lastName,
          suffix: person.suffix,
          ssn: person.maskedSsn || person.ssn, // Use masked SSN
          date_of_birth: person.dateOfBirth,
          dateOfBirth: person.dateOfBirth,
          gender: person.gender,
          ethnicity: person.ethnicity,
          primaryPhone: person.primaryPhone,
          secondaryPhone: person.secondaryPhone,
          email: person.email,
          preferredSpokenLanguage: person.preferredSpokenLanguage,
          preferredWrittenLanguage: person.preferredWrittenLanguage,
          residenceAddressLine1: person.residenceAddressLine1,
          residenceAddressLine2: person.residenceAddressLine2,
          residenceCity: person.residenceCity,
          residenceState: person.residenceState,
          residenceZip: person.residenceZip,
          countyOfResidence: person.countyOfResidence,
          guardianConservatorName: person.guardianConservatorName,
          guardianConservatorAddress: person.guardianConservatorAddress,
          guardianConservatorPhone: person.guardianConservatorPhone,
          disasterPreparednessCode: person.disasterPreparednessCode,
          status: 'active',
        }));
        setSearchResults(mappedResults);
        setShowResults(true);
      } else {
        setSearchError(result.error || 'Search failed');
      }
    } catch (error) {
      console.error('Search error:', error);
      setSearchError('Network error occurred during search');
    } finally {
      setSearchLoading(false);
    }
  };

  const resetSearch = () => {
    setSearchForm({
      lastName: '',
      firstName: '',
      ssn: '',
      cin: '',
      personType: '',
      providerNumber: '',
      dateOfBirth: '',
      gender: '',
      districtOffice: '',
      streetNumber: '',
      streetName: '',
      unitType: '',
      unitNumber: '',
      city: '',
    });
    setSearchResults([]);
    setSearchError(null);
    setShowResults(false);
    setSsnVerificationStatus('idle');
    setSsnVerificationMessage('');
  };

  const selectPerson = (person: any) => {
    console.log('Selected person:', person);
    // If person is found, populate form with all available data
    setApplicationForm({
      ...applicationForm,
      appFirstName: person.firstName || person.first_name || '',
      appLastName: person.lastName || person.last_name || '',
      appMiddleName: person.middleName || person.middle_name || '',
      appSuffix: person.suffix || '',
      ssn: person.ssn || '', // Note: SSN may be masked, user may need to re-enter
        dateOfBirth: person.dateOfBirth || person.date_of_birth || '',
        gender: person.gender || '',
        ethnicity: person.ethnicity || '',
        ageGroup: person.ageGroup || calculateAgeGroup(person.dateOfBirth || person.date_of_birth) || '',
        primaryPhone: person.primaryPhone || '',
      secondaryPhone: person.secondaryPhone || '',
      emailAddress: person.email || '',
      preferredSpokenLanguage: person.preferredSpokenLanguage || '',
      preferredWrittenLanguage: person.preferredWrittenLanguage || '',
      residenceAddressLine1: person.residenceAddressLine1 || '',
      residenceAddressLine2: person.residenceAddressLine2 || '',
      residenceCity: person.residenceCity || '',
      residenceState: person.residenceState || 'CA',
      residenceZip: person.residenceZip || '',
      countyOfResidence: person.countyOfResidence || '',
      guardianConservatorName: person.guardianConservatorName || '',
      guardianConservatorAddress: person.guardianConservatorAddress || '',
      guardianConservatorPhone: person.guardianConservatorPhone || '',
      disasterPreparednessCode: person.disasterPreparednessCode || '',
    });
    // Open create application modal with existing person
    setShowCreateApplicationModal(true);
    setCurrentStep(1);
    // Store personId for case creation (if using existing person)
    (window as any).selectedPersonId = person.personId || person.id;
    setSsnVerificationStatus('idle');
    setSsnVerificationMessage('');
  };

  useEffect(() => {
    if (!authLoading && !isAuthenticated) {
      router.replace('/login');
    }
  }, [authLoading, isAuthenticated, router]);

  if (authLoading || !isAuthenticated) {
    return (
      <div className="container py-5 text-center">
        <p>Loading...</p>
      </div>
    );
  }

  return (
    <div className={styles.pageContainer}>
      <div className="container">
        <Breadcrumb path={breadcrumbPath} currentPage={currentPage} />

        <div className={styles.applicationContent}>
          <div className={styles.pageHeader}>
            <h1>Person Search</h1>
          </div>

          <div className={styles.searchCriteria}>
            <h2>Search Criteria</h2>

            <div className={styles.criteriaSections}>
              {/* General Section */}
              <div className={styles.criteriaSection}>
                <h3>General</h3>
                <div className={styles.formGrid}>
                  <div className={styles.formGroup}>
                    <label htmlFor="lastName">Last Name</label>
                    <input
                      type="text"
                      id="lastName"
                      className={styles.formInput}
                      value={searchForm.lastName}
                      onChange={(e) => setSearchForm({ ...searchForm, lastName: e.target.value })}
                    />
                  </div>
                  <div className={styles.formGroup}>
                    <label htmlFor="firstName">First Name</label>
                    <input
                      type="text"
                      id="firstName"
                      className={styles.formInput}
                      value={searchForm.firstName}
                      onChange={(e) => setSearchForm({ ...searchForm, firstName: e.target.value })}
                    />
                  </div>
                  <div className={styles.formGroup}>
                    <label htmlFor="ssn">SSN</label>
                    <input
                      type="text"
                      id="ssn"
                      className={styles.formInput}
                      value={searchForm.ssn}
                      onChange={(e) => setSearchForm({ ...searchForm, ssn: e.target.value })}
                    />
                  </div>
                  <div className={styles.formGroup}>
                    <label htmlFor="cin">CIN</label>
                    <input
                      type="text"
                      id="cin"
                      className={styles.formInput}
                      value={searchForm.cin}
                      onChange={(e) => setSearchForm({ ...searchForm, cin: e.target.value })}
                    />
                  </div>
                  <div className={styles.formGroup}>
                    <label htmlFor="dateOfBirth">Date Of Birth</label>
                    <input
                      type="date"
                      id="dateOfBirth"
                      className={styles.formInput}
                      value={searchForm.dateOfBirth}
                      onChange={(e) => setSearchForm({ ...searchForm, dateOfBirth: e.target.value })}
                    />
                  </div>
                  <div className={styles.formGroup}>
                    <label htmlFor="gender">Gender</label>
                    <select
                      id="gender"
                      className={styles.formInput}
                      value={searchForm.gender}
                      onChange={(e) => setSearchForm({ ...searchForm, gender: e.target.value })}
                    >
                      <option value="">Select Gender</option>
                      <option value="male">Male</option>
                      <option value="female">Female</option>
                      <option value="other">Other</option>
                    </select>
                  </div>
                </div>
              </div>
            </div>

            <div className={styles.formActions}>
              <button
                className={`${styles.btn} ${styles.btnPrimary}`}
                onClick={performSearch}
                disabled={searchLoading}
              >
                {searchLoading ? 'Searching...' : 'Search'}
              </button>
              <button className={`${styles.btn} ${styles.btnSecondary}`} onClick={resetSearch}>
                Reset
              </button>
              <button
                className={`${styles.btn} ${styles.btnSuccess}`}
                onClick={openCreateApplicationModal}
              >
                Continue Application
              </button>
            </div>
          </div>
        </div>

        {/* Search Results Section */}
        <div className={styles.searchResultsSection}>
          <div className={styles.resultsHeader}>
            <h2>Search Results</h2>
            <span className={styles.resultsCount}>
              {showResults ? `${searchResults.length} results found` : '0 results found'}
            </span>
          </div>

          <div className={styles.resultsContainer}>
            {searchError ? (
              <div className={styles.errorMessage}>
                <p>Error: {searchError}</p>
              </div>
            ) : !showResults ? (
              <div className={styles.noResults}>
                <p>No search results to display. Please enter search criteria and click "Search" to find matching records.</p>
              </div>
            ) : searchResults.length === 0 ? (
              <div className={styles.noResults}>
                <p>No matching records found. You can proceed to create a new case by clicking "Continue Application" button.</p>
              </div>
            ) : (
              <div className={styles.resultsTableContainer}>
                <table className={styles.resultsTable}>
                  <thead>
                    <tr>
                      <th>Name</th>
                      <th>SSN</th>
                      <th>Date of Birth</th>
                      <th>Gender</th>
                      <th>Status</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {searchResults.map((person, index) => (
                      <tr key={index}>
                        <td>
                          {person.first_name} {person.middle_name || ''} {person.last_name}{' '}
                          {person.suffix || ''}
                        </td>
                        <td>***-**-{person.ssn?.slice(-4) || ''}</td>
                        <td>{person.date_of_birth}</td>
                        <td>{person.gender || 'N/A'}</td>
                        <td>
                          <span
                            className={`${styles.statusBadge} ${
                              person.status === 'active' ? styles.statusActive : styles.statusInactive
                            }`}
                          >
                            {person.status}
                          </span>
                        </td>
                        <td>
                          <button
                            className={`${styles.actionBtn} ${styles.btnPrimary}`}
                            onClick={() => selectPerson(person)}
                          >
                            Select
                          </button>
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

      {/* Create Application Modal */}
      {showCreateApplicationModal && (
        <div className={styles.modalOverlay} onClick={closeCreateApplicationModal}>
          <div className={styles.modalContent} onClick={(e) => e.stopPropagation()}>
            <div className={styles.modalHeader}>
              <h2>Create Application</h2>
              <div className={styles.modalControls}>
                <button className={styles.infoBtn} title="Information">
                  ℹ️
                </button>
                <button className={styles.closeBtn} onClick={closeCreateApplicationModal}>
                  ×
                </button>
              </div>
            </div>

            <div className={styles.modalBody}>
              {/* Step Indicator */}
              <div className={styles.stepIndicator}>
                <div className={`${styles.step} ${currentStep >= 1 ? styles.active : ''}`}>
                  <span className={styles.stepNumber}>1</span>
                  <span className={styles.stepLabel}>Create Application</span>
                </div>
                <div className={styles.stepConnector}></div>
                <div className={`${styles.step} ${currentStep >= 2 ? styles.active : ''}`}>
                  <span className={styles.stepNumber}>2</span>
                  <span className={styles.stepLabel}>Create Case</span>
                </div>
              </div>

              {/* Step 1: Create Application */}
              {currentStep === 1 && (
                <div className={styles.stepContent}>
                  <h3>Recipient Information</h3>
                  <div className={styles.formSections}>
                    {/* Personal Information Section */}
                    <div className={styles.formSection}>
                      <h4>Personal Information</h4>
                      <div className={styles.formGrid}>
                        <div className={styles.formGroup}>
                          <label htmlFor="appFirstName">First Name <span className={styles.required}>*</span></label>
                          <input
                            type="text"
                            id="appFirstName"
                            className={styles.formInput}
                            value={applicationForm.appFirstName}
                            onChange={(e) => {
                              setApplicationForm({ ...applicationForm, appFirstName: e.target.value });
                              setSsnVerificationStatus('idle');
                              setSsnVerificationMessage('');
                            }}
                            required
                          />
                        </div>
                        <div className={styles.formGroup}>
                          <label htmlFor="appMiddleName">Middle Name</label>
                          <input
                            type="text"
                            id="appMiddleName"
                            className={styles.formInput}
                            value={applicationForm.appMiddleName}
                            onChange={(e) =>
                              setApplicationForm({ ...applicationForm, appMiddleName: e.target.value })
                            }
                          />
                        </div>
                        <div className={styles.formGroup}>
                          <label htmlFor="appLastName">Last Name <span className={styles.required}>*</span></label>
                          <input
                            type="text"
                            id="appLastName"
                            className={styles.formInput}
                            value={applicationForm.appLastName}
                            onChange={(e) =>
                              setApplicationForm({ ...applicationForm, appLastName: e.target.value })
                            }
                            required
                          />
                        </div>
                        <div className={styles.formGroup}>
                          <label htmlFor="appSuffix">Suffix</label>
                          <input
                            type="text"
                            id="appSuffix"
                            className={styles.formInput}
                            placeholder="Jr., Sr., III, etc."
                            value={applicationForm.appSuffix}
                            onChange={(e) =>
                              setApplicationForm({ ...applicationForm, appSuffix: e.target.value })
                            }
                          />
                        </div>
                        <div className={styles.formGroup}>
                          <label htmlFor="ssn">SSN <span className={styles.required}>*</span></label>
                          <div className={styles.ssnInputGroup}>
                            <input
                              type="text"
                              id="ssn"
                              className={styles.formInput}
                              placeholder="XXX-XX-XXXX"
                              pattern="\d{3}-\d{2}-\d{4}"
                              value={applicationForm.ssn}
                              onChange={(e) => {
                                setApplicationForm({ ...applicationForm, ssn: e.target.value });
                                setSsnVerificationStatus('idle');
                                setSsnVerificationMessage('');
                              }}
                              required
                            />
                            <button
                              type="button"
                              className={styles.verifyBtn}
                              onClick={verifySsn}
                              disabled={verifyingSsn}
                            >
                              {verifyingSsn ? 'Verifying...' : 'Verify'}
                            </button>
                          </div>
                          {ssnVerificationStatus === 'success' && (
                            <span className={styles.verificationSuccess}>{ssnVerificationMessage}</span>
                          )}
                          {ssnVerificationStatus === 'error' && (
                            <span className={styles.verificationError}>{ssnVerificationMessage}</span>
                          )}
                        </div>
                        <div className={styles.formGroup}>
                          <label htmlFor="dateOfBirth">Date of Birth <span className={styles.required}>*</span></label>
                          <input
                            type="date"
                            id="dateOfBirth"
                            className={styles.formInput}
                            value={applicationForm.dateOfBirth}
                            onChange={(e) => {
                              const dob = e.target.value;
                              const ageGroup = calculateAgeGroup(dob);
                              setApplicationForm({ 
                                ...applicationForm, 
                                dateOfBirth: dob,
                                ageGroup: ageGroup
                              });
                            }}
                            required
                          />
                        </div>
                        <div className={styles.formGroup}>
                          <label htmlFor="ageGroup">Age Group</label>
                          <input
                            type="text"
                            id="ageGroup"
                            className={styles.formInput}
                            value={applicationForm.ageGroup}
                            readOnly
                            placeholder="Auto-calculated from Date of Birth"
                            style={{ backgroundColor: '#f5f5f5', cursor: 'not-allowed' }}
                          />
                          <small style={{ color: '#666', fontSize: '0.875rem' }}>
                            Automatically calculated from date of birth
                          </small>
                        </div>
                        <div className={styles.formGroup}>
                          <label htmlFor="gender">Gender</label>
                          <select
                            id="gender"
                            className={styles.formInput}
                            value={applicationForm.gender}
                            onChange={(e) =>
                              setApplicationForm({ ...applicationForm, gender: e.target.value })
                            }
                          >
                            <option value="">Select Gender</option>
                            <option value="Male">Male</option>
                            <option value="Female">Female</option>
                            <option value="Non-Binary">Non-Binary</option>
                            <option value="Prefer Not to Say">Prefer Not to Say</option>
                            <option value="Other">Other</option>
                          </select>
                        </div>
                        <div className={styles.formGroup}>
                          <label htmlFor="ethnicity">Ethnicity</label>
                          <select
                            id="ethnicity"
                            className={styles.formInput}
                            value={applicationForm.ethnicity}
                            onChange={(e) =>
                              setApplicationForm({ ...applicationForm, ethnicity: e.target.value })
                            }
                          >
                            <option value="">Select Ethnicity</option>
                            <option value="Hispanic/Latino">Hispanic/Latino</option>
                            <option value="White">White</option>
                            <option value="Black/African American">Black/African American</option>
                            <option value="Asian">Asian</option>
                            <option value="Native American">Native American</option>
                            <option value="Pacific Islander">Pacific Islander</option>
                            <option value="Other">Other</option>
                            <option value="Prefer Not to Say">Prefer Not to Say</option>
                          </select>
                        </div>
                      </div>
                    </div>

                    {/* Contact Information Section */}
                    <div className={styles.formSection}>
                      <h4>Contact Information</h4>
                      <div className={styles.formGrid}>
                        <div className={styles.formGroup}>
                          <label htmlFor="primaryPhone">Primary Phone</label>
                          <input
                            type="tel"
                            id="primaryPhone"
                            className={styles.formInput}
                            placeholder="(XXX) XXX-XXXX"
                            value={applicationForm.primaryPhone}
                            onChange={(e) =>
                              setApplicationForm({ ...applicationForm, primaryPhone: e.target.value })
                            }
                          />
                        </div>
                        <div className={styles.formGroup}>
                          <label htmlFor="secondaryPhone">Secondary Phone</label>
                          <input
                            type="tel"
                            id="secondaryPhone"
                            className={styles.formInput}
                            placeholder="(XXX) XXX-XXXX"
                            value={applicationForm.secondaryPhone}
                            onChange={(e) =>
                              setApplicationForm({ ...applicationForm, secondaryPhone: e.target.value })
                            }
                          />
                        </div>
                        <div className={styles.formGroup}>
                          <label htmlFor="emailAddress">Email</label>
                          <input
                            type="email"
                            id="emailAddress"
                            className={styles.formInput}
                            value={applicationForm.emailAddress}
                            onChange={(e) =>
                              setApplicationForm({ ...applicationForm, emailAddress: e.target.value })
                            }
                          />
                        </div>
                        <div className={styles.formGroup}>
                          <label htmlFor="preferredSpokenLanguage">Preferred Spoken Language</label>
                          <input
                            type="text"
                            id="preferredSpokenLanguage"
                            className={styles.formInput}
                            value={applicationForm.preferredSpokenLanguage}
                            onChange={(e) =>
                              setApplicationForm({ ...applicationForm, preferredSpokenLanguage: e.target.value })
                            }
                          />
                        </div>
                        <div className={styles.formGroup}>
                          <label htmlFor="preferredWrittenLanguage">Preferred Written Language</label>
                          <input
                            type="text"
                            id="preferredWrittenLanguage"
                            className={styles.formInput}
                            value={applicationForm.preferredWrittenLanguage}
                            onChange={(e) =>
                              setApplicationForm({ ...applicationForm, preferredWrittenLanguage: e.target.value })
                            }
                          />
                        </div>
                      </div>
                    </div>

                    {/* Residence Address Section */}
                    <div className={styles.formSection}>
                      <h4>Residence Address <span className={styles.required}>*</span></h4>
                      <div className={styles.formGrid}>
                        <div className={styles.formGroup} style={{ gridColumn: '1 / -1' }}>
                          <label htmlFor="residenceAddressLine1">Address Line 1 <span className={styles.required}>*</span></label>
                          <input
                            type="text"
                            id="residenceAddressLine1"
                            className={styles.formInput}
                            value={applicationForm.residenceAddressLine1}
                            onChange={(e) =>
                              setApplicationForm({ ...applicationForm, residenceAddressLine1: e.target.value })
                            }
                            required
                          />
                        </div>
                        <div className={styles.formGroup} style={{ gridColumn: '1 / -1' }}>
                          <label htmlFor="residenceAddressLine2">Address Line 2</label>
                          <input
                            type="text"
                            id="residenceAddressLine2"
                            className={styles.formInput}
                            value={applicationForm.residenceAddressLine2}
                            onChange={(e) =>
                              setApplicationForm({ ...applicationForm, residenceAddressLine2: e.target.value })
                            }
                          />
                        </div>
                        <div className={styles.formGroup}>
                          <label htmlFor="residenceCity">City <span className={styles.required}>*</span></label>
                          <input
                            type="text"
                            id="residenceCity"
                            className={styles.formInput}
                            value={applicationForm.residenceCity}
                            onChange={(e) =>
                              setApplicationForm({ ...applicationForm, residenceCity: e.target.value })
                            }
                            required
                          />
                        </div>
                        <div className={styles.formGroup}>
                          <label htmlFor="residenceState">State <span className={styles.required}>*</span></label>
                          <input
                            type="text"
                            id="residenceState"
                            className={styles.formInput}
                            value={applicationForm.residenceState}
                            onChange={(e) =>
                              setApplicationForm({ ...applicationForm, residenceState: e.target.value })
                            }
                            required
                          />
                        </div>
                        <div className={styles.formGroup}>
                          <label htmlFor="residenceZip">ZIP Code <span className={styles.required}>*</span></label>
                          <input
                            type="text"
                            id="residenceZip"
                            className={styles.formInput}
                            pattern="[0-9]{5}(-[0-9]{4})?"
                            value={applicationForm.residenceZip}
                            onChange={(e) =>
                              setApplicationForm({ ...applicationForm, residenceZip: e.target.value })
                            }
                            required
                          />
                        </div>
                        <div className={styles.formGroup}>
                          <label htmlFor="countyOfResidence">County of Residence <span className={styles.required}>*</span></label>
                          <input
                            type="text"
                            id="countyOfResidence"
                            className={styles.formInput}
                            value={applicationForm.countyOfResidence}
                            onChange={(e) =>
                              setApplicationForm({ ...applicationForm, countyOfResidence: e.target.value })
                            }
                            required
                          />
                        </div>
                      </div>
                    </div>

                    {/* Mailing Address Section */}
                    <div className={styles.formSection}>
                      <h4>Mailing Address</h4>
                      <div className={styles.formGroup}>
                        <label>
                          <input
                            type="checkbox"
                            checked={applicationForm.mailingSameAsResidence}
                            onChange={(e) =>
                              setApplicationForm({ ...applicationForm, mailingSameAsResidence: e.target.checked })
                            }
                          />
                          {' '}Same as Residence Address
                        </label>
                      </div>
                      {!applicationForm.mailingSameAsResidence && (
                        <div className={styles.formGrid}>
                          <div className={styles.formGroup} style={{ gridColumn: '1 / -1' }}>
                            <label htmlFor="mailingAddressLine1">Address Line 1</label>
                            <input
                              type="text"
                              id="mailingAddressLine1"
                              className={styles.formInput}
                              value={applicationForm.mailingAddressLine1}
                              onChange={(e) =>
                                setApplicationForm({ ...applicationForm, mailingAddressLine1: e.target.value })
                              }
                            />
                          </div>
                          <div className={styles.formGroup} style={{ gridColumn: '1 / -1' }}>
                            <label htmlFor="mailingAddressLine2">Address Line 2</label>
                            <input
                              type="text"
                              id="mailingAddressLine2"
                              className={styles.formInput}
                              value={applicationForm.mailingAddressLine2}
                              onChange={(e) =>
                                setApplicationForm({ ...applicationForm, mailingAddressLine2: e.target.value })
                              }
                            />
                          </div>
                          <div className={styles.formGroup}>
                            <label htmlFor="mailingCity">City</label>
                            <input
                              type="text"
                              id="mailingCity"
                              className={styles.formInput}
                              value={applicationForm.mailingCity}
                              onChange={(e) =>
                                setApplicationForm({ ...applicationForm, mailingCity: e.target.value })
                              }
                            />
                          </div>
                          <div className={styles.formGroup}>
                            <label htmlFor="mailingState">State</label>
                            <input
                              type="text"
                              id="mailingState"
                              className={styles.formInput}
                              value={applicationForm.mailingState}
                              onChange={(e) =>
                                setApplicationForm({ ...applicationForm, mailingState: e.target.value })
                              }
                            />
                          </div>
                          <div className={styles.formGroup}>
                            <label htmlFor="mailingZip">ZIP Code</label>
                            <input
                              type="text"
                              id="mailingZip"
                              className={styles.formInput}
                              pattern="[0-9]{5}(-[0-9]{4})?"
                              value={applicationForm.mailingZip}
                              onChange={(e) =>
                                setApplicationForm({ ...applicationForm, mailingZip: e.target.value })
                              }
                            />
                          </div>
                        </div>
                      )}
                    </div>

                    {/* Guardian/Conservator Section */}
                    <div className={styles.formSection}>
                      <h4>Guardian/Conservator Information (Optional)</h4>
                      <div className={styles.formGrid}>
                        <div className={styles.formGroup}>
                          <label htmlFor="guardianConservatorName">Name</label>
                          <input
                            type="text"
                            id="guardianConservatorName"
                            className={styles.formInput}
                            value={applicationForm.guardianConservatorName}
                            onChange={(e) =>
                              setApplicationForm({ ...applicationForm, guardianConservatorName: e.target.value })
                            }
                          />
                        </div>
                        <div className={styles.formGroup} style={{ gridColumn: '1 / -1' }}>
                          <label htmlFor="guardianConservatorAddress">Address</label>
                          <input
                            type="text"
                            id="guardianConservatorAddress"
                            className={styles.formInput}
                            value={applicationForm.guardianConservatorAddress}
                            onChange={(e) =>
                              setApplicationForm({ ...applicationForm, guardianConservatorAddress: e.target.value })
                            }
                          />
                        </div>
                        <div className={styles.formGroup}>
                          <label htmlFor="guardianConservatorPhone">Phone</label>
                          <input
                            type="tel"
                            id="guardianConservatorPhone"
                            className={styles.formInput}
                            placeholder="(XXX) XXX-XXXX"
                            value={applicationForm.guardianConservatorPhone}
                            onChange={(e) =>
                              setApplicationForm({ ...applicationForm, guardianConservatorPhone: e.target.value })
                            }
                          />
                        </div>
                      </div>
                    </div>

                    {/* Disaster Preparedness Section */}
                    <div className={styles.formSection}>
                      <h4>Disaster Preparedness Code (Optional)</h4>
                      <div className={styles.formGroup}>
                        <label htmlFor="disasterPreparednessCode">3-Letter Code</label>
                        <input
                          type="text"
                          id="disasterPreparednessCode"
                          className={styles.formInput}
                          placeholder="AAA"
                          pattern="[A-Z]{3}"
                          maxLength={3}
                          style={{ textTransform: 'uppercase' }}
                          value={applicationForm.disasterPreparednessCode}
                          onChange={(e) =>
                            setApplicationForm({ ...applicationForm, disasterPreparednessCode: e.target.value.toUpperCase() })
                          }
                        />
                        <small>Format: 3 uppercase letters (e.g., AAB)</small>
                      </div>
                    </div>
                  </div>
                </div>
              )}

              {/* Step 2: Create Case */}
              {currentStep === 2 && (
                <div className={styles.stepContent}>
                  <h3>Case Information</h3>
                  <div className={styles.formSection}>
                    <h4>Case Details</h4>
                    <div className={styles.formGrid}>
                      <div className={styles.formGroup} style={{ gridColumn: '1 / -1' }}>
                        <label htmlFor="ihssReferralData">Case Notes / IHSS Referral Data</label>
                        <textarea
                          id="ihssReferralData"
                          className={styles.formInput}
                          rows={4}
                          value={caseForm.ihssReferralData}
                          onChange={(e) =>
                            setCaseForm({ ...caseForm, ihssReferralData: e.target.value })
                          }
                          placeholder="Enter any additional case notes or referral information..."
                        />
                      </div>
                      <div className={styles.formGroup} style={{ gridColumn: '1 / -1' }}>
                        <label htmlFor="additionalInfo">Additional Information</label>
                        <textarea
                          id="additionalInfo"
                          className={styles.formInput}
                          rows={3}
                          value={applicationForm.additionalInfo}
                          onChange={(e) =>
                            setApplicationForm({ ...applicationForm, additionalInfo: e.target.value })
                          }
                          placeholder="Any additional information about the case..."
                        />
                      </div>
                    </div>
                    <div className={styles.infoBox}>
                      <p><strong>Note:</strong> Case will be created with:</p>
                      <ul>
                        <li>County: {applicationForm.countyOfResidence || 'Not specified'}</li>
                        <li>CMIPS Case Number: Will be auto-generated</li>
                        <li>Legacy Case Number: Will be auto-generated</li>
                        <li>Status: PENDING</li>
                      </ul>
                    </div>
                  </div>
                </div>
              )}

              {/* Modal Actions */}
              <div className={styles.modalActions}>
                {currentStep === 1 ? (
                  <>
                    <button
                      className={`${styles.btn} ${styles.btnSecondary}`}
                      onClick={closeCreateApplicationModal}
                    >
                      Cancel
                    </button>
                    <button 
                      className={`${styles.btn} ${styles.btnSuccess}`} 
                      onClick={nextStep}
                      disabled={!applicationForm.appFirstName || !applicationForm.appLastName || !applicationForm.ssn || !applicationForm.dateOfBirth || !applicationForm.residenceAddressLine1 || !applicationForm.residenceCity || !applicationForm.residenceZip || !applicationForm.countyOfResidence}
                    >
                      Next: Case Details
                    </button>
                  </>
                ) : (
                  <>
                    <button className={`${styles.btn} ${styles.btnSecondary}`} onClick={previousStep}>
                      Previous
                    </button>
                    <button className={`${styles.btn} ${styles.btnSuccess}`} onClick={submitCase}>
                      Submit
                    </button>
                  </>
                )}
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}


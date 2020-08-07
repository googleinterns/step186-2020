// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

// This file provides the JavaScript induced on the landing page (index.html)

//
// Event listener registration
//

// Hooks the onLoad function to the DOMContentLoaded event.
document.addEventListener('DOMContentLoaded', onLoad);

//
// Functions
//

/**
 * Fires as soon as the DOM is loaded.
 */
async function onLoad() {
  const collegeLocations = await (await fetch('./assets/college-locations4.json')).json();

  // Add all colleges as datalist options.
  // We use document fragments because the DOM is slow if we add
  // each option individually and let the DOM update in between.
  let frag = document.createDocumentFragment();
  const collegeDataList = document.getElementById('colleges');
  collegeLocations.forEach((location) => {
    const newOption = document.createElement('option');
    newOption.textContent = location.NAME;
    newOption.setAttribute('data-value', location.UNITID);

    frag.appendChild(newOption);
  });

  collegeDataList.appendChild(frag);

  // When users select an option from the dropdown, send them to that page.
  collegeDataList.addEventListener('input', navigateUserToCollegePage);
}

/**
 * When the user selects a college from the dropdown, we immediately
 * navigate them to the appropriate college's page.
 */
function navigateUserToCollegePage() {
  const collegename = document.getElementById('colleges').value;
  const collegeid = document.querySelector(`#colleges option[value=${collegename}]`).dataset.value;
  window.location.href = `/find-events.html?collegeid=${collegeid}`;
}

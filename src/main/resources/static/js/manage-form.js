(async () => {
    'use strict'

    // Fetch all the forms we want to apply custom Bootstrap validation styles to
    const forms = document.querySelectorAll('.needs-validation')

    // Loop over them and prevent submission
    Array.from(forms).forEach(form => {
        form.addEventListener('submit', event => {
            if (!form.checkValidity()) {
                event.preventDefault()
                event.stopPropagation()
            }

            form.classList.add('was-validated')
        }, false)
    })

    let rows;
    let headers;
    const groups = await (await fetch("/api/v1/field-groups")).json();

    const downloadZipButton = document.getElementById("download-zip-button");

    // Logo elements
    const logoReader = new FileReader();
    const logoPicker = document.getElementById("upload-logo");
    const logoPreview = document.getElementById("logo-preview");
    const logoCard = document.getElementById("logo-card");
    const buttonClearInputLogo = document.getElementById("button-clear-input-logo");

    // CSV elements
    const csvReader = new FileReader();
    const csvPicker = document.getElementById("upload-csv");
    const buttonClearInputCsv = document.getElementById("button-clear-input-csv");
    const csvValidationElement = document.getElementById("upload-csv-validation");

    // Form elements
    const mapSection = document.getElementById("map-section");
    const billForm = document.getElementById("bill-form");

    const show = (element) => {
        element.style.display = "block";
    }
    const hide = (element) => {
        element.style.display = "none";
    }

    // *****************************************************************************************************************
    // Manage logo
    // *****************************************************************************************************************
    logoPicker.onchange = () => logoReader.readAsDataURL(logoPicker.files[0]);

    const clearLogo = () => {
        hide(logoCard);
        logoPicker.value = "";
        logoPreview.src = "";
    }

    buttonClearInputLogo.onclick = clearLogo;

    logoReader.onloadend = () => {
        logoPreview.src = logoReader.result;
        show(logoCard);
    }

    // *****************************************************************************************************************
    // Manage form
    // *****************************************************************************************************************
    const createGenerateButton = (loading = false) => {
        const buttonText = "Generate bills";

        const generateBillsButton = document.createElement("button");
        generateBillsButton.classList.add("btn");
        generateBillsButton.classList.add("btn-primary");
        generateBillsButton.setAttribute("type", "submit");

        if (loading) {
            const spinnerElement = document.createElement("span");
            spinnerElement.classList.add("spinner-border");
            spinnerElement.classList.add("spinner-border-sm");
            spinnerElement.setAttribute("role", "status");

            generateBillsButton.disabled = true;
            generateBillsButton.append(spinnerElement);
            generateBillsButton.append(buttonText);
        } else {
            generateBillsButton.innerHTML = buttonText;
        }

        return generateBillsButton;
    }

    const addButtons = () => {
        const generateButtonContainer = document.createElement("div");
        generateButtonContainer.append(createGenerateButton());
        mapSection.append(generateButtonContainer);
    }

    const addNewGroup = (name, label, elements, addLineAtEnd = true) => {
        const groupName = document.createElement("h4");
        groupName.id = name;
        groupName.classList.add("mb-3");
        groupName.innerHTML = label;

        const groupElement = document.createElement("div");
        groupElement.classList.add("row");
        groupElement.classList.add("g-3");
        groupElement.append(...elements);

        mapSection.append(groupName);
        mapSection.append(groupElement);
        if (addLineAtEnd) {
            const line = document.createElement("hr");
            line.classList.add("my-4");
            mapSection.append(line);
        }
    }

    const createOptionElement = (name, selected = false) => new Option(name, name, selected, selected);

    const createSelectFieldElement = ({
                                          name,
                                          label,
                                          defaultMap,
                                          required,
                                          requiredText,
                                          defaultValue
                                      }, options) => {
        const containerElement = document.createElement("div");
        containerElement.classList.add("col-12");

        const labelElement = document.createElement("label");
        labelElement.setAttribute("for", name);
        labelElement.classList.add("form-label");
        labelElement.classList.add("col-sm-6");
        labelElement.classList.add("float-start");
        if (required) {
            labelElement.classList.add("required");
        }
        labelElement.innerHTML = `${label} &#8212; <code>${defaultMap}</code>`;
        containerElement.append(labelElement);
        if (defaultValue) {
            const defaultValueElement = document.createElement("span");
            defaultValueElement.classList.add("col-sm-6");
            defaultValueElement.classList.add("float-end");
            defaultValueElement.classList.add("default-value");
            defaultValueElement.innerHTML = defaultValue;
            containerElement.append(defaultValueElement);
        }

        const selectElement = document.createElement("select");
        selectElement.id = name;
        selectElement.classList.add("form-select");
        selectElement.required = required;
        if (options.includes(defaultMap)) {
            selectElement.append(createOptionElement(undefined, false));
            options.map(option => selectElement.append(createOptionElement(option, option === defaultMap)));
        } else {
            selectElement.append(createOptionElement(undefined, true), ...options.map(option => createOptionElement(option)));
        }
        containerElement.append(selectElement);

        if (required) {
            const requiredElement = document.createElement("div");
            requiredElement.classList.add("invalid-feedback");
            requiredElement.innerHTML = requiredText;
            containerElement.append(requiredElement);
        }

        return containerElement;
    }

    const showCsvError = () => {
        csvValidationElement.classList.add("d-block");
    }

    const hideCsvError = () => {
        csvValidationElement.classList.remove("d-block");
    }

    const clearCsv = () => {
        hide(mapSection);
        csvPicker.value = "";
        mapSection.innerHTML = '';
    }

    buttonClearInputCsv.onclick = clearCsv;

    csvPicker.onchange = () => csvReader.readAsText(csvPicker.files[0]);
    csvReader.onloadend = () => {
        hide(mapSection);
        rows = CSV.parse(csvReader.result);
        if (!rows || !rows.length) {
            console.error("Invalid CSV");
            rows = undefined;
            showCsvError();
        }
        hideCsvError();

        headers = rows[0];
        if (!headers || !headers.length) {
            console.error("Invalid CSV header");
            headers = undefined;
            showCsvError();
        }
        headers = headers.map(header => header.toLowerCase());

        // Remove headers row
        rows = rows.slice(1);

        if (!rows || !rows.length) {
            console.error("No CSV data");
            rows = undefined;
            showCsvError();
        }

        hideCsvError();

        groups.map(({
                        name,
                        label,
                        fields
                    }) => addNewGroup(name, label, fields.map(field => createSelectFieldElement(field, headers))));

        addButtons();

        show(mapSection);

    };

    const preventProcessForm = async (e) => {
        if (e.preventDefault) {
            e.preventDefault();
        }

        const generateButtonContainer = mapSection.lastElementChild;
        generateButtonContainer.replaceChildren(createGenerateButton(true));

        if (billForm.checkValidity()) {
            const fieldsMap = groups.flatMap(({fields}) => fields.map(({name}) => ({
                [name]: {
                    mapWith: document.getElementById(name).value,
                    staticValue: null // TODO: static value for each bill
                }
            }))).reduce((accumulator, currentValue) => ({...accumulator, ...currentValue}), {});

            const csv = rows.map(row => row.reduce((accumulator, currentValue, currentIndex) => ({
                ...accumulator,
                [headers[currentIndex]]: currentValue?.toString()
            }), {}));

            const data = {
                fieldsMap,
                logoBase64: !!logoPicker.value ? logoPreview.src : null,
                csv
            };

            const response = await fetch('/api/v1/generate/bills', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(data)
            });

            if (response.status === 200) {
                const bytes = await response.blob();

                downloadZipButton.href = URL.createObjectURL(bytes);
                downloadZipButton.setAttribute('download', "bills.zip");
                downloadZipButton.click();
            }
        }

        generateButtonContainer.replaceChildren(createGenerateButton());

        // You must return false to prevent the default form behavior
        return false;
    }
    if (billForm.attachEvent) {
        billForm.attachEvent("submit", preventProcessForm);
    } else {
        billForm.addEventListener("submit", preventProcessForm);
    }
})()

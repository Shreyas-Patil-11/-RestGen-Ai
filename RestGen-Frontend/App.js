 // Add Field Row
    function addField() {
      const container = document.getElementById("fields");
      const div = document.createElement("div");
      div.className = "field-row";
      div.innerHTML = `
        <input type="text" placeholder="Field Name" required />
        <select>
          <option>String</option>
          <option>Integer</option>
          <option>Long</option>
          <option>Double</option>
          <option>Boolean</option>
          <option>Float</option>
          <option>Byte</option>
          <option>Short</option>
          <option>Char</option>
          <option>Date</option>
        </select>
        <label><input type="checkbox" class="ck-pk" /> Primary Key</label>
        <label><input type="checkbox" class="ck-nullable" /> Nullable</label>
        <label><input type="checkbox" class="ck-unique" /> Unique</label>
      `;
      container.appendChild(div);
    }

    // Bind Add Field button
    document.getElementById("addFieldBtn").addEventListener("click", addField);

    // Handle form submit
    document.getElementById("apiForm").onsubmit = async function (e) {
      e.preventDefault();

      const projectName = document.getElementById("projectName").value.trim();
      const tableName = document.getElementById("tableName").value.trim();
      const packageName = document.getElementById("packageName").value.trim();
      const fields = [];

      // Validate basic inputs
      if (!projectName || !tableName || !packageName) {
        alert("Please fill Project Name, Table Name, and Package Name.");
        return;
      }

      // Collect all field rows
      const rows = document.querySelectorAll("#fields .field-row");
      if (rows.length === 0) {
        alert("Please add at least one field.");
        return;
      }

      rows.forEach((row) => {
        const name = row.querySelector("input[type='text']").value.trim();
        const type = row.querySelector("select").value;

        const primaryKey = !!row.querySelector(".ck-pk")?.checked;
        const nullable = !!row.querySelector(".ck-nullable")?.checked;
        const unique = !!row.querySelector(".ck-unique")?.checked;

        if (name) {
          fields.push({ name, type, primaryKey, nullable, unique });
        }
      });

      if (fields.length === 0) {
        alert("Please provide at least one valid field name.");
        return;
      }

      // Show loader immediately
      let loader = document.getElementById("loader");
      if (!loader) {
        loader = document.createElement("div");
        loader.id = "loader";
        loader.innerText = "⏳ Generating project... Please wait.";
        loader.style.position = "fixed";
        loader.style.top = "50%";
        loader.style.left = "50%";
        loader.style.transform = "translate(-50%, -50%)";
        loader.style.padding = "20px 30px";
        loader.style.backgroundColor = "#333";
        loader.style.color = "#fff";
        loader.style.borderRadius = "8px";
        loader.style.boxShadow = "0 4px 10px rgba(0,0,0,0.3)";
        loader.style.zIndex = "1000";
        document.body.appendChild(loader);
      } else {
        loader.style.display = "block";
      }

      try {
        const response = await fetch("http://localhost:8080/api/projects/generate", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ projectName, tableName, packageName, fields }),
        });

        if (!response.ok) {
          // Try to read error JSON; fallback to text
          let errorMessage = "Unknown error";
          try {
            const errorData = await response.json();
            errorMessage = errorData.error || JSON.stringify(errorData);
          } catch {
            errorMessage = await response.text();
          }
          alert("Error: " + errorMessage);
          loader.style.display = "none";
          return;
        }

        // Download the ZIP
        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement("a");
        a.href = url;
        a.download = (projectName || "project") + ".zip";
        document.body.appendChild(a);
        a.click();
        a.remove();
        window.URL.revokeObjectURL(url);
      } catch (err) {
        alert("Network or server error: " + err.message);
      } finally {
        // Hide loader after completion / error
        const l = document.getElementById("loader");
        if (l) l.style.display = "none";
      }
    };
#!/usr/bin/env python3

import csv
import math

# -----------------------------
# Configuration
# -----------------------------
TOTAL_USERS = 2000
USERS_PER_COMPANY = 100
OUTPUT_FILE = "users_2000.csv"

companies = [
    ("Acme Corp", "acme.com"),
    ("Globex", "globex.com"),
    ("Initech", "initech.io"),
    ("Umbrella Corp", "umbrella.co"),
    ("Stark Industries", "starkindustries.com"),
    ("Wayne Enterprises", "wayneenterprises.com"),
    ("Hooli", "hooli.com"),
    ("Soylent", "soylent.com"),
    ("Oscorp", "oscorp.com"),
    ("Cyberdyne Systems", "cyberdyne.ai"),
    ("Vault-Tec", "vaulttec.com"),
    ("Aperture Science", "aperture.io"),
    ("Massive Dynamic", "massivedynamic.com"),
    ("Tyrell Corp", "tyrell.com"),
    ("Zenith Tech", "zenithtech.com"),
    ("Orion Tech", "oriontech.io"),
    ("Nexus Systems", "nexussys.com"),
    ("Monarch Solutions", "monarchsolutions.com"),
    ("Black Mesa", "blackmesa.org"),
    ("Wonka Industries", "wonka.co"),
]

first_names = [
    "Alex", "Sarah", "Daniel", "Emily", "Michael", "Olivia", "James", "Emma",
    "William", "Ava", "Ethan", "Mia", "Noah", "Isabella", "Liam", "Sophia",
    "Lucas", "Charlotte", "Henry", "Amelia", "Benjamin", "Harper", "Elijah",
    "Evelyn", "Jacob", "Abigail", "Matthew", "Ella", "David", "Grace",
    "Joseph", "Chloe", "Samuel", "Lily", "Andrew", "Hannah", "Christopher",
    "Natalie", "Joshua", "Zoe", "Ryan", "Leah", "Nathan", "Sofia",
    "Jonathan", "Victoria", "Aaron", "Madison", "Caleb", "Penelope"
]

last_names = [
    "Johnson", "Miller", "Smith", "Davis", "Brown", "Wilson", "Taylor",
    "Anderson", "Thomas", "Moore", "Martin", "Jackson", "White", "Harris",
    "Clark", "Lewis", "Walker", "Hall", "Allen", "Young", "King", "Wright",
    "Lopez", "Hill", "Scott", "Green", "Adams", "Baker", "Nelson", "Carter",
    "Mitchell", "Perez", "Roberts", "Turner", "Phillips", "Campbell",
    "Parker", "Evans", "Edwards", "Collins"
]

departments = [
    "Engineering", "Product", "QA", "IT",
    "HR", "Security", "Operations", "Finance"
]

roles_by_department = {
    "Engineering": "Software Engineer",
    "Product": "Product Manager",
    "QA": "QA Engineer",
    "IT": "Systems Engineer",
    "HR": "HR Specialist",
    "Security": "Security Analyst",
    "Operations": "Operations Manager",
    "Finance": "Financial Analyst",
}


def get_app_role(profile_id: int) -> str:
    mod = profile_id % 10
    if mod == 0:
        return "ROLE_ADMIN"
    elif mod == 1:
        return "ROLE_EDITOR"
    elif mod == 2:
        return "ROLE_REVIEWER"
    else:
        return "ROLE_USER"


# -----------------------------
# CSV Generation
# -----------------------------
with open(OUTPUT_FILE, "w", newline="", encoding="utf-8") as csvfile:
    writer = csv.writer(csvfile)
    writer.writerow([
        "Profile #",
        "Names",
        "Email",
        "Company",
        "Department",
        "Role",
        "App_Role"
    ])

    for profile_id in range(1, TOTAL_USERS + 1):
        company_index = (profile_id - 1) // USERS_PER_COMPANY
        company, domain = companies[company_index]

        first_name = first_names[(profile_id - 1) % len(first_names)]
        last_name = last_names[(profile_id - 1) % len(last_names)]
        full_name = f"{first_name} {last_name}"

        email = f"{first_name.lower()}.{last_name.lower()}@{domain}"

        department = departments[profile_id % len(departments)]
        role = roles_by_department[department]
        app_role = get_app_role(profile_id)

        writer.writerow([
            profile_id,
            full_name,
            email,
            company,
            department,
            role,
            app_role
        ])

print(f"Generated {TOTAL_USERS} users in '{OUTPUT_FILE}'")
